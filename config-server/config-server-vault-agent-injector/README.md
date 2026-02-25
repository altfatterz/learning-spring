## Config Server with Vault Agent Injector 

Vault Agent Injector docs: https://developer.hashicorp.com/vault/docs/platform/k8s/injector

### Create a Kubernetes cluster:

```bash
$ k3d cluster create
$ kubectl cluster-info
Kubernetes control plane is running at https://0.0.0.0:49315
CoreDNS is running at https://0.0.0.0:49315/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://0.0.0.0:49315/api/v1/namespaces/kube-system/services/https:metrics-server:https/proxy

$ kubectl get nodes
NAME                       STATUS   ROLES                  AGE   VERSION
k3d-k3s-default-server-0   Ready    control-plane,master   34s   v1.31.5+k3s1
```

### Create the config ConfigMap

```bash
$ cd k8s
$ kubectl create configmap service-configs \
  --from-file=config-server-client-dev.properties \
  --from-file=config-server-client-prd.properties
 
```

### Build a docker image and import to k8s-cluster

```bash
./build.sh 
```

### Install config-server

```bash
$ kubectl apply -f k8s/k8s.yaml
```

### Test it

```bash
$ kubectl get pods 
NAME                                                 READY   STATUS    RESTARTS   AGE
config-server-vault-agent-injector-bc466f96f-b5rb4   2/2     Running   0          2m18s

$ kubectl exec -it config-server-vault-agent-injector-bc466f96f-b5rb4 -c busybox -- ls -l /app/data/config
lrwxrwxrwx    1 root     root            42 Feb 25 11:06 config-server-client-dev.properties -> ..data/config-server-client-dev.properties
lrwxrwxrwx    1 root     root            42 Feb 25 11:06 config-server-client-prd.properties -> ..data/config-server-client-prd.properties

$ kubectl port-forward svc/config-server-vault-agent-injector 8080:8080

# in another terminal 
$ http :8080/config-server-client/dev
$ http :8080/config-server-client/prd

```

## Using Vault Agent Sidecar Injector

#### Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update

# install Vault in development mode
$ helm install my-vault hashicorp/vault --set "server.dev.enabled=true"

$ kubectl get pod | grep my-vault
my-vault-0                                           1/1     Running   0             27s
my-vault-agent-injector-7bbb9c49cf-n46bw             1/1     Running   0             27s

$ kubectl get svc | grep my-vault
my-vault                             ClusterIP   10.43.187.12   <none>        8200/TCP,8201/TCP   16s
my-vault-agent-injector-svc          ClusterIP   10.43.77.135   <none>        443/TCP             16s
my-vault-internal                    ClusterIP   None           <none>        8200/TCP,8201/TCP   16s

```

### Access the Vault UI

```bash
$ kubectl port-forward svc/my-vault 8200:8200
# token is 'root', see logs: kubectl logs -f my-vault-0 
$ open http://localhost:8200/ui 
```

### Configure Vault - Enable AppRole authentication

```bash
$ kubectl exec -it my-vault-0 -- sh
$ vault --version
Vault v1.21.2 (781ba452d731fe2d59ccbc1b37ca7c5a18edb998), built 2026-01-06T08:33:05Z
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="root"

$ vault status
Key             Value
---             -----
Seal Type       shamir
Initialized     true
Sealed          false
Total Shares    1
Threshold       1
Version         1.21.2
Build Date      2026-01-06T08:33:05Z
Storage Type    inmem
Cluster Name    vault-cluster-6aa59333
Cluster ID      333d02ce-8dc8-6730-98e1-de455e918e9e
HA Enabled      false

# list vault secrets 
$ vault secrets list
Path          Type         Accessor              Description
----          ----         --------              -----------
cubbyhole/    cubbyhole    cubbyhole_b669a6ad    per-token private secret storage
identity/     identity     identity_c960ad88     identity store
secret/       kv           kv_a92e3a4a           key/value secret storage
sys/          system       system_6df0606a       system endpoints used for control, policy and debugging

# Enable AppRole auth method
$ vault auth enable approle
Success! Enabled approle auth method at: approle/

$ vault auth list
Path        Type       Accessor                 Description                Version
----        ----       --------                 -----------                -------
approle/    approle    auth_approle_96839831    n/a                        n/a
token/      token      auth_token_4d454c2b      token based credentials    n/a
```

### Create Secret in Vault

```bash
$ vault kv put -mount=secret config-server-client/dev message="Dev Secret in Vault!"
$ vault kv put -mount=secret config-server-client/prd message="Prod Secret in Vault!"
$ vault kv get -mount=secret config-server-client/dev
============ Secret Path ============
secret/data/config-server-client/dev

======= Metadata =======
Key                Value
---                -----
created_time       2026-02-25T13:17:47.786206429Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

===== Data =====
Key        Value
---        -----
message    Dev Secret in Vault!
```

### Get RoleId and SecretId for config-server to authenticate with Vault

```bash
# create the config-server-policy
$ vault policy write config-server-policy -<<EOF
# Read-only permission on secrets stored at 'secret/data/config-server-client/dev'
path "secret/data/config-server-client/*" {
  capabilities = [ "read" ]
}
path "secret/data/config-server-client" {
  capabilities = [ "read" ]
} 
EOF

$ vault policy list
$ vault policy read config-server-policy

# create the config-server-role
$ vault write auth/approle/role/config-server-role token_policies="config-server-policy" token_ttl=2h token_max_ttl=4h

$ vault list auth/approle/role
$ vault read auth/approle/role/config-server-role

# get role-id
$ vault read auth/approle/role/config-server-role/role-id
Key        Value
---        -----
role_id    f509afc2-01ed-6247-9d44-df1e1dbc0098

# get secret-id
$ vault write -force auth/approle/role/config-server-role/secret-id
Key                   Value
---                   -----
secret_id             a533dfa1-6da2-8b7d-2361-8f4a3ddc1639
secret_id_accessor    dae63620-b365-420b-7df1-e33433739058
secret_id_num_uses    0
secret_id_ttl         0s

# test login with role-id and secret-id 
$ kubectl exec -it my-vault-0 -- sh
$ export VAULT_ADDR="http://127.0.0.1:8200"
$ vault kv get secret/config-server-client/dev
URL: GET http://127.0.0.1:8200/v1/sys/internal/ui/mounts/secret/data/config-server-client/dev

# login will generate a token
$ vault write auth/approle/login role_id="f509afc2-01ed-6247-9d44-df1e1dbc0098" secret_id="a533dfa1-6da2-8b7d-2361-8f4a3ddc1639"
Key                     Value
---                     -----
token                   hvs.CAESIKyKVcg55Y7s-52ap-fD6b48Sp60xltrbQ1fQ7923uFEGh4KHGh2cy5ON1h6WU1IQlRkcmxTcXY2N3BYZU1ySUQ
token_accessor          9w4kTCWG4aPIqytNtSVmxJ0d
token_duration          2h
token_renewable         true
token_policies          ["config-server-policy" "default"]
identity_policies       []
policies                ["config-server-policy" "default"]
token_meta_role_name    config-server-role

$ export APP_TOKEN="hvs.CAESIKyKVcg55Y7s-52ap-fD6b48Sp60xltrbQ1fQ7923uFEGh4KHGh2cy5ON1h6WU1IQlRkcmxTcXY2N3BYZU1ySUQ"
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/config-server-client/dev
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/config-server-client/prd
```

### Create k8s secret with role-id and secret-id

```bash
$ kubectl create secret generic config-server-approle-creds \
  --from-literal=role-id="f509afc2-01ed-6247-9d44-df1e1dbc0098" \
  --from-literal=secret-id="a533dfa1-6da2-8b7d-2361-8f4a3ddc1639"
```

### Inject the vault-agent

```bash
$ kubectl apply -f k8s/k8s-vault-agent-with-approle.yaml
# test connection from vault agent
$ k exec -it config-server-vault-agent-injector-86d58c5ffd-q98ml -c vault-agent -- sh
# check that the role-id and secret-id were written to the disk
$ ls -l /vault/custom
# after login the token was written here
$ cat /home/vault/.vault-token
# set the VAULT_ADDR
$ export VAULT_ADDR=http://my-vault:8200
# test that we can query a secret
$ vault kv get secret/config-server-client/prd
```

### Test from outside

```bash
# notice the config-server does not query the vault secrets
$ http :8080/config-server-client/dev
$ http :8080/config-server-client/prd
```

