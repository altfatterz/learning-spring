## Config Server with Vault Agent Init

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
$ kubectl create cm service-configs-vault-agent-init \
  --from-file=k8s/configs/account-service-dev.properties \
  --from-file=k8s/configs/account-service-prd.properties \
  --from-file=k8s/configs/payment-service-dev.properties \
  --from-file=k8s/configs/payment-service-prd.properties
$ kubectl get cm service-configs-vault-agent-init -o yaml  
```

#### Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update

# install Vault in development mode
$ helm install my-vault hashicorp/vault --set "server.dev.enabled=true"

$ kubectl get pod | grep my-vault
my-vault-0                                           1/1     Running   0             27s
my-vault-agent-init-7bbb9c49cf-n46bw                 1/1     Running   0             27s

$ kubectl get svc | grep my-vault
my-vault                             ClusterIP   10.43.187.12   <none>        8200/TCP,8201/TCP   16s
my-vault-agent-init-svc              ClusterIP   10.43.77.135   <none>        443/TCP             16s
my-vault-internal                    ClusterIP   None           <none>        8200/TCP,8201/TCP   16s
```

### Configure Vault - Enable AppRole authentication

```bash
$ kubectl exec -it my-vault-0 -- sh
$ vault --version
Vault v1.21.2 (781ba452d731fe2d59ccbc1b37ca7c5a18edb998), built 2026-01-06T08:33:05Z
# VAULT_ADDR is already set to http://127.0.0.1:8200
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

### Access the Vault UI

```bash
$ kubectl port-forward svc/my-vault 8200:8200
# token is 'root', see logs: kubectl logs -f my-vault-0 
$ open http://localhost:8200/ui 
```

### Get RoleId and SecretId for the vault-agent-init container to authenticate with Vault

```bash
# create the config-server-policy
$ vault policy write config-server-policy-vault-agent-init -<<EOF
path "secret/data/account-service" {
  capabilities = ["read"]
}
path "secret/data/account-service/*" {
  capabilities = [ "read" ]
}
path "secret/data/payment-service" {
  capabilities = ["read"]
}
path "secret/data/payment-service/*" {
  capabilities = [ "read" ]
}
# needed because of spring-cloud-config-server
path "secret/data/application" {
  capabilities = ["read"]
}
path "secret/data/application/*" {
  capabilities = [ "read" ]
}
EOF

$ vault policy list
$ vault policy read config-server-policy-vault-agent-init

# create the config-server-role-vault-agent-init
$ vault write auth/approle/role/config-server-role-vault-agent-init token_policies="config-server-policy-vault-agent-init" token_ttl=8h token_max_ttl=10h

$ vault list auth/approle/role
$ vault read auth/approle/role/config-server-role-vault-agent-init

# get role-id
$ vault read auth/approle/role/config-server-role-vault-agent-init/role-id
Key        Value
---        -----
role_id    f742469b-c05b-fc73-3b28-62921c055259

# get secret-id
$ vault write -force auth/approle/role/config-server-role-vault-agent-init/secret-id
Key                   Value
---                   -----
secret_id             f82c6f09-b1bb-7739-4e2d-abaeff858ed6
secret_id_accessor    25eee428-dfc8-ad90-b9b3-bfecd0fa138a
secret_id_num_uses    0
secret_id_ttl         0s

# test login with role-id and secret-id 
$ kubectl exec -it my-vault-0 -- sh

# login will generate a token
$ vault write auth/approle/login role_id="f742469b-c05b-fc73-3b28-62921c055259" secret_id="f82c6f09-b1bb-7739-4e2d-abaeff858ed6"
Key                     Value
---                     -----
token                   hvs.CAESIPHAHuBoO0b39nBmKUKmfP95mvxGTerrh7zX1g9ZMD5TGh4KHGh2cy54SVhqRnIzeUZXS3BEYWVvSENsbDlWQ0o
token_accessor          EnjiNZVJLprvE3cSnLYudYSw
token_duration          8h
token_renewable         true
token_policies          ["config-server-policy-vault-agent-init" "default"]
identity_policies       []
policies                ["config-server-policy-vault-agent-init" "default"]
token_meta_role_name    config-server-role-vault-agent-init

$ export APP_TOKEN="hvs.CAESIPHAHuBoO0b39nBmKUKmfP95mvxGTerrh7zX1g9ZMD5TGh4KHGh2cy54SVhqRnIzeUZXS3BEYWVvSENsbDlWQ0o"
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/account-service/dev
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/account-service/prd
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/payment-service/dev
$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/payment-service/prd
```

### Create k8s secret with role-id and secret-id

```bash
# will be used in the deployment
$ kubectl create secret generic config-server-approle-creds-vault-agent-init \
  --from-literal=role-id="f742469b-c05b-fc73-3b28-62921c055259" \
  --from-literal=secret-id="f82c6f09-b1bb-7739-4e2d-abaeff858ed6"
$ kubectl get secret config-server-approle-creds-vault-agent-init -o yaml
```

### Build a docker image and import to k8s-cluster

```bash
$ ./build.sh 
```

### Create Secret in Vault

```bash
$ vault kv put -mount=secret account-service/dev message="Account Service Dev Secret in Vault!"
$ vault kv put -mount=secret account-service/prd message="Account Service Prd Secret in Vault!"

$ vault kv put -mount=secret payment-service/dev message="Payment Service Dev Secret in Vault!"
$ vault kv put -mount=secret payment-service/prd message="Payment Service Prd Secret in Vault!"

$ vault kv get -mount=secret account-service/dev
$ vault kv get -mount=secret payment-service/dev
```

### Install config-server 

```bash
# we are not using the injector only the vault-agent-init container
$ kubectl apply -f k8s/k8s.yaml
```

### Test

```bash
$ kubectl get pods 
NAME                                                 READY   STATUS    RESTARTS   AGE
config-server-vault-agent-init-bc466f96f-b5rb4       2/2     Running   0          2m18s

# Test that config is present
$ kubectl exec -it $(kubectl get pods -l app=config-server-vault-agent-init -o name) -c busybox -- ls -l /app/data/config

# Test that secret is present
$ kubectl exec -it $(kubectl get pods -l app=config-server-vault-agent-init -o name) -c busybox -- ls -l /vault/secrets

# check the logs of the vault-agent-init container
$ kubectl logs -f $(kubectl get pods -l app=config-server-vault-agent-init -o name) -c vault-agent-init 

# port forward
$ kubectl port-forward svc/config-server-vault-agent-init 8080:8080

# in another terminal 
$ http :8080/account-service/dev
$ http :8080/account-service/prd
$ http :8080/payment-service/dev
$ http :8080/payment-service/prd
```

