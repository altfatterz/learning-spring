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
$ kubeclt port-forward svc/vault-ui 8200:8200 -n vault
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

Vault Secrets Operator can be configured to maintain an internal, encrypted persistent cache of client tokens.

This is helpful for being able to transparently renew leases for dynamic secrets should the operator restart.

```yaml
defaultVaultConnection:
  enabled: true
  address: "http://vault.vault.svc.cluster.local:8200"
  skipTLSVerify: false
controller:
  manager:
    clientCache:
      persistenceModel: direct-encrypted
      storageEncryption:
        enabled: true
        mount: demo-auth-mount
        keyName: vso-client-cache
        transitMount: demo-transit
        kubernetes:
          role: auth-role-operator
          serviceAccount: vault-secrets-operator-controller-manager
          tokenAudiences: ["vault"]
```

```bash
$ kubectl exec -it vault-0 -n vault -- sh
# We need a Transit Secrets Engine.
$ vault secrets enable -path=demo-transit transit
# Success! Enabled the transit secrets engine at: demo-transit/
$ vault secrets list
# Create a encryption key.
$ vault write -force demo-transit/keys/vso-client-cache
# Success! Data written to: demo-transit/keys/vso-client-cache
$ vault read demo-transit/keys/vso-client-cache
# Create a policy for the operator role to access the encryption key.
$ vault policy write demo-auth-policy-operator - <<EOF
path "demo-transit/encrypt/vso-client-cache" {
   capabilities = ["create", "update"]
}
path "demo-transit/decrypt/vso-client-cache" {
   capabilities = ["create", "update"]
}
EOF
# Success! Uploaded policy: demo-auth-policy-operator
$ vault policy list
# Create Kubernetes auth role for the operator.
$ vault write auth/demo-auth-mount/role/auth-role-operator \
   bound_service_account_names=vault-secrets-operator-controller-manager \
   bound_service_account_namespaces=vault-secrets-operator-system \
   token_ttl=0 \
   token_period=120 \
   token_policies=demo-auth-policy-operator \
   audience=vault
# Success! Data written to: auth/demo-auth-mount/role/auth-role-operator
```

Install the Secrets Operator

```bash
$ helm install vault-secrets-operator hashicorp/vault-secrets-operator -n vault-secrets-operator-system --create-namespace --values vault-operator-values.yaml
```

Review the CRDs:

```bash
$ kubectl get crds | grep hashicorp

hcpauths.secrets.hashicorp.com                2024-10-24T11:57:47Z
hcpvaultsecretsapps.secrets.hashicorp.com     2024-10-24T11:57:47Z
secrettransformations.secrets.hashicorp.com   2024-10-24T11:57:47Z
vaultauthglobals.secrets.hashicorp.com        2024-10-24T11:57:47Z
vaultauths.secrets.hashicorp.com              2024-10-24T11:57:47Z
vaultconnections.secrets.hashicorp.com        2024-10-24T11:57:47Z
vaultdynamicsecrets.secrets.hashicorp.com     2024-10-24T11:57:48Z
vaultpkisecrets.secrets.hashicorp.com         2024-10-24T11:57:48Z
vaultstaticsecrets.secrets.hashicorp.com      2024-10-24T11:57:48Z
```

```bash
$ kubectl get pods --all-namespaces | grep vault
vault                           vault-0                                                     1/1     Running     0          122m
vault-secrets-operator-system   vault-secrets-operator-controller-manager-5fd78476f-kvnqd   2/2     Running     0          102s
```


