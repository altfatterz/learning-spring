## The Vault Secrets Operator on Kubernetes


### Create k8s cluster

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
$ kubectl get nodes
NAME                       STATUS   ROLES                  AGE   VERSION
k3d-k8s-cluster-server-0   Ready    control-plane,master   26s   v1.28.8+k3s1
```

### Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update
$ helm search repo hashicorp/vault

NAME                            	CHART VERSION	APP VERSION	DESCRIPTION
hashicorp/vault                 	0.28.1       	1.17.2     	Official HashiCorp Vault Chart
hashicorp/vault-secrets-operator	0.9.0        	0.9.0      	Official Vault Secrets Operator Chart

$ helm install vault hashicorp/vault -n vault --create-namespace --values vault-values.yaml
$ helm list -n vault
NAME 	NAMESPACE	REVISION	UPDATED                              	STATUS  	CHART       	APP VERSION
vault	vault    	1       	2024-10-24 11:57:09.337941 +0200 CEST	deployed	vault-0.28.1	1.17.2
$ helm status vault -n vault
$ helm get manifest vault -n vault
$ kubectl get pods -n vault 
NAME      READY   STATUS    RESTARTS   AGE
vault-0   1/1     Running   0          6m5s
```

Check Vault UI:

```bash
$ kubectl port-forward svc/vault-ui 8200:8200 -n vault
$ open http://localhost:8200
```

### Configure Vault

Configure Kubernetes Authentication, create a new kv-2 secrets engine, create a policy, a role and a static secret.

```bash
$ kubectl exec -it vault-0 -n vault -- sh
$ vault auth list
Path      Type     Accessor               Description                Version
----      ----     --------               -----------                -------
token/    token    auth_token_e154fb27    token based credentials    n/a
$ vault auth enable -path demo-auth-mount kubernetes
$ vault auth list
Path                Type          Accessor                    Description                Version
----                ----          --------                    -----------                -------
demo-auth-mount/    kubernetes    auth_kubernetes_e503fef3    n/a                        n/a
token/              token         auth_token_e154fb27         token based credentials    n/a
# Configure the auth method.
$ vault write auth/demo-auth-mount/config kubernetes_host="https://$KUBERNETES_PORT_443_TCP_ADDR:443"
$ vault read auth/demo-auth-mount/config
Key                                  Value
---                                  -----
disable_iss_validation               true
disable_local_ca_jwt                 false
issuer                               n/a
kubernetes_ca_cert                   n/a
kubernetes_host                      https://10.43.0.1:443
pem_keys                             []
token_reviewer_jwt_set               false
use_annotations_as_alias_metadata    false
# Enable a new kv-v2 secrets Engine
$ vault secrets enable -path=kvv2 kv-v2
$ vault secrets list
Path          Type         Accessor              Description
----          ----         --------              -----------
cubbyhole/    cubbyhole    cubbyhole_005d642c    per-token private secret storage
identity/     identity     identity_27a7c79b     identity store
kvv2/         kv           kv_e7326e2b           n/a
secret/       kv           kv_a3e3b415           key/value secret storage
sys/          system       system_124af3db       system endpoints used for control, policy and debugging
```

Create a policy

```bash
$ cd /tmp
$ tee webapp.json <<EOF
path "kvv2/data/webapp/config" {
   capabilities = ["read", "list"]
}
EOF
$ vault policy write webapp webapp.json
$ vault policy list
$ vault policy read webapp
```

Create a role

```bash
$ vault write auth/demo-auth-mount/role/role1 \
   bound_service_account_names=demo-static-app \
   bound_service_account_namespaces=app \
   policies=webapp \
   audience=vault \
   ttl=24h
Success! Data written to: auth/demo-auth-mount/role/role1 
$ vault read auth/demo-auth-mount/role/role1
Key                                         Value
---                                         -----
alias_name_source                           serviceaccount_uid
audience                                    vault
bound_service_account_names                 [demo-static-app]
bound_service_account_namespace_selector    n/a
bound_service_account_namespaces            [app]
policies                                    [webapp]
token_bound_cidrs                           []
token_explicit_max_ttl                      0s
token_max_ttl                               0s
token_no_default_policy                     false
token_num_uses                              0
token_period                                0s
token_policies                              [webapp]
token_ttl                                   24h
token_type                                  default
ttl                                         24h 
```

Create a secret

```bash
$ vault kv put kvv2/webapp/config username="static-user" password="static-password"
$ vault kv get kvv2/webapp/config
===== Secret Path =====
kvv2/data/webapp/config

======= Metadata =======
Key                Value
---                -----
created_time       2024-10-24T10:21:10.749816171Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

====== Data ======
Key         Value
---         -----
password    static-password
username    static-user
```

### Install the Vault Secrets Operator

Check the `vault-operator-values.yaml`

```bash
$ helm install vault-secrets-operator hashicorp/vault-secrets-operator -n vault-secrets-operator-system --create-namespace --values vault-operator-values.yaml
```

Review the CRDs:

```bash
$ kubectl get crds | grep hashicorp

hcpauths.secrets.hashicorp.com                2024-10-25T13:48:46Z
hcpvaultsecretsapps.secrets.hashicorp.com     2024-10-25T13:48:46Z
secrettransformations.secrets.hashicorp.com   2024-10-25T13:48:46Z
vaultauthglobals.secrets.hashicorp.com        2024-10-25T13:48:46Z
vaultauths.secrets.hashicorp.com              2024-10-25T13:48:46Z
vaultconnections.secrets.hashicorp.com        2024-10-25T13:48:46Z
vaultdynamicsecrets.secrets.hashicorp.com     2024-10-25T13:48:46Z
vaultpkisecrets.secrets.hashicorp.com         2024-10-25T13:48:46Z
vaultstaticsecrets.secrets.hashicorp.com      2024-10-25T13:48:46Z
```

```bash
$ kubectl get pods --all-namespaces | grep vault
vault                           vault-0                                                     1/1     Running     0          122m
vault-secrets-operator-system   vault-secrets-operator-controller-manager-5fd78476f-kvnqd   2/2     Running     0          102s
```

### Deploy and sync a secret

```
# Create a namespace called 'app' 
$ kubectl create ns app
# Set up Kubernetes authentication for the secret.
$ kubectl apply -f vault-auth-static.yaml
$ kubectl describe vaultauth static-auth -n app
Name:         static-auth
Namespace:    app
Labels:       <none>
Annotations:  <none>
API Version:  secrets.hashicorp.com/v1beta1
Kind:         VaultAuth
Metadata:
  Creation Timestamp:  2024-10-25T13:50:40Z
  Finalizers:
    vaultauth.secrets.hashicorp.com/finalizer
  Generation:        1
  Resource Version:  984
  UID:               c7a8bfbc-6283-4ea2-8242-e966c08e3840
Spec:
  Kubernetes:
    Audiences:
      vault
    Role:                      role1
    Service Account:           demo-static-app
    Token Expiration Seconds:  600
  Method:                      kubernetes
  Mount:                       demo-auth-mount
Status:
  Spec Hash:  661c57ea3d9f706b38193a7659b40060607d3560c6e28477a0e433d100636ad4
  Valid:      true
Events:
  Type    Reason    Age   From       Message
  ----    ------    ----  ----       -------
  Normal  Accepted  61s   VaultAuth  Successfully handled VaultAuth resource request
```

# Create a 'secretkv' in the app namespace.

```bash
$ kubectl apply -f static-secret.yaml
$ kubectl describe vaultstaticsecret vault-kv-app -n app
Name:         vault-kv-app
Namespace:    app
Labels:       <none>
Annotations:  <none>
API Version:  secrets.hashicorp.com/v1beta1
Kind:         VaultStaticSecret
Metadata:
  Creation Timestamp:  2024-10-25T13:53:39Z
  Finalizers:
    vaultstaticsecret.secrets.hashicorp.com/finalizer
  Generation:        2
  Resource Version:  1135
  UID:               21b5eb20-7ce5-483d-b477-91c2e09bffd0
Spec:
  Destination:
    Create:     true
    Name:       secretkv
    Overwrite:  false
    Transformation:
  Hmac Secret Data:  true
  Mount:             kvv2
  Path:              webapp/config
  Refresh After:     30s
  Type:              kv-v2
  Vault Auth Ref:    static-auth
Status:
  Last Generation:  2
  Secret MAC:       vo7gRUvdQ7e5jp+i3YIXVJRN0NQAgeBTUf6eJv7QlOQ=
Events:
  Type    Reason         Age   From               Message
  ----    ------         ----  ----               -------
  Normal  SecretSynced   102s  VaultStaticSecret  Secret synced
  Normal  SecretRotated  102s  VaultStaticSecret  Secret synced
```

The corresponding secret was created: 

```bash
$ kubectl get secret secretkv -o yaml -n app
apiVersion: v1
data:
  _raw: eyJkYXRhIjp7InBhc3N3b3JkIjoic3RhdGljLXBhc3N3b3JkIiwidXNlcm5hbWUiOiJzdGF0aWMtdXNlciJ9LCJtZXRhZGF0YSI6eyJjcmVhdGVkX3RpbWUiOiIyMDI0LTEwLTI1VDEzOjQ4OjI2LjM5NzgwMzA2N1oiLCJjdXN0b21fbWV0YWRhdGEiOm51bGwsImRlbGV0aW9uX3RpbWUiOiIiLCJkZXN0cm95ZWQiOmZhbHNlLCJ2ZXJzaW9uIjoxfX0=
  password: c3RhdGljLXBhc3N3b3Jk
  username: c3RhdGljLXVzZXI=
kind: Secret
type: Opaque
```

If you change the secret in Vault the corresponding Kubernetes secret will be changed.

### Resources:

https://developer.hashicorp.com/vault/tutorials/kubernetes/vault-secrets-operator
