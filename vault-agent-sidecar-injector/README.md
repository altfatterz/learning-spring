# Vault Agent Injector https://developer.hashicorp.com/vault/docs/platform/k8s/injector

## Try locally the application

### Start PostgreSQL

```bash
$ docker compose up -d
```

### Build and start the application `DemoApp`

```bash
$ mvn clean package spring-boot:run
```

### Access the endpoint:

```bash
$ http :8080/customers\?lastName=Doe

[
    {
        "id": 1,
        "name": "John Doe"
    },
    {
        "id": 2,
        "name": "Jane Doe"
    }
]
```

## Running on Kubernetes

### Create a Kubernetes cluster:

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
Kubernetes control plane is running at https://0.0.0.0:60607
CoreDNS is running at https://0.0.0.0:60607/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://0.0.0.0:60607/api/v1/namespaces/kube-system/services/https:metrics-server:https/proxy

$ kubectl get nodes
NAME                       STATUS   ROLES                  AGE     VERSION
k3d-k8s-cluster-server-0   Ready    control-plane,master   3m19s   v1.30.6+k3s1
```

### Install PostgreSQL:

```bash
$ helm repo add bitnami https://charts.bitnami.com/bitnami 
$ helm repo update
$ helm install my-postgresql bitnami/postgresql -f postgresql-values.yaml
$ helm status my-postgresql
```

### Test connection to PostgreSQL:

```bash
$ kubectl port-forward svc/my-postgresql 5432:5432
$ PGPASSWORD=secret psql -h localhost -U postgres -d postgres -p 5432
```

### Build a docker image and import to k8s-cluster

```bash
./build.sh 
```

### Install the app

```bash
$ kubectl apply -f k8s/k8s.yaml
```

### Test it

```bash
$ kubectl get pods 

my-postgresql-0                                      1/1     Running   0          20m
vault-agent-sidecar-injector-demo-84b9f8cff6-fshm8   1/1     Running   0          2m29s

$ kubectl port-forward svc/vault-agent-sidecar-injector-demo 8080:8080
$ http :8080/customers\?lastName=Doe

HTTP/1.1 200 OK
Content-Type: application/json
transfer-encoding: chunked

[
    {
        "id": 1,
        "name": "John Doe"
    },
    {
        "id": 2,
        "name": "Jane Doe"
    }
]
```

## Using Vault Agent Sidecar Injector with Kubernetes authentication

#### Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update

# install Vault in development mode
$ helm install my-vault hashicorp/vault --set "server.dev.enabled=true"

$ kubectl get pod | grep my-vault
my-vault-0                                           1/1     Running   0          42s
my-vault-agent-injector-7b6f54669c-jcmp7             1/1     Running   0          42s

$ kubectl get svc | grep my-vault
my-vault                            ClusterIP   10.43.95.194    <none>        8200/TCP,8201/TCP   64s
my-vault-agent-injector-svc         ClusterIP   10.43.99.66     <none>        443/TCP             64s
my-vault-internal                   ClusterIP   None            <none>        8200/TCP,8201/TCP   64

```

### Access the Vault UI

```bash
$ kubectl port-forward svc/my-vault 8200:8200
# token is 'root', see logs: kubectl logs -f my-vault-0 
$ open http://localhost:8200/ui 
```

### Add the databases connection details into Vault (using the /secret secret engine)

```bash
$ kubectl exec -it my-vault-0 -- sh

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
Cluster Name    vault-cluster-43602883
Cluster ID      1a623632-3f9c-1d8e-470d-cd239034b65a
HA Enabled      false

# display information about the locally authenticated token 
$ vault token lookup
Key                 Value
---                 -----
accessor            TLsscovKm6xnFhdGG0jCQMiE
creation_time       1738336145
creation_ttl        0s
display_name        token
entity_id           n/a
expire_time         <nil>
explicit_max_ttl    0s
id                  root
issue_time          2025-01-31T15:09:05.924497755Z
meta                <nil>
num_uses            0
orphan              true
path                auth/token/create
policies            [root]
renewable           false
ttl                 0s
type                service

# with secrets with field names containing '-' had issues with agent-inject-template. more details: https://support.hashicorp.com/hc/en-us/articles/15667022913299-Vault-Agent-throws-bad-character-U-002D
$ vault kv put -mount=secret vault-agent-sidecar-injector-demo db_username=postgres db_password=secret db_url=r2dbc:postgresql://my-postgresql:5432/postgres
$ vault kv get -mount=secret vault-agent-sidecar-injector-demo

================ Secret Path ================
secret/data/vault-agent-sidecar-injector-demo

======= Metadata =======
Key                Value
---                -----
created_time       2026-02-23T20:00:44.495404094Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1
/ $ vault kv get -mount=secret vault-agent-sidecar-injector-demo
================ Secret Path ================
secret/data/vault-agent-sidecar-injector-demo

======= Metadata =======
Key                Value
---                -----
created_time       2026-02-23T20:00:44.495404094Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

======= Data =======
Key            Value
---            -----
db_password    secret
db_url         r2dbc:postgresql://my-postgresql:5432/postgres
db_username    postgres
```

You can get this information also with http/curl like:

```bash
$ http :8200/v1/secret/data/vault-agent-sidecar-injector-demo "X-Vault-Token: root"

{
    "auth": null,
    "data": {
        "data": {
            "db_password": "secret",
            "db_url": "r2dbc:postgresql://my-postgresql:5432/postgres",
            "db_username": "postgres"
        },
        "metadata": {
            "created_time": "2025-01-31T15:12:35.345207457Z",
            "custom_metadata": null,
            "deletion_time": "",
            "destroyed": false,
            "version": 1
        }
    },
    "lease_duration": 0,
    "lease_id": "",
    "mount_type": "kv",
    "renewable": false,
    "request_id": "0dc90cb0-08ef-92a0-7294-6d14a96e82d3",
    "warnings": null,
    "wrap_info": null
}
```

### Configure Kubernetes authentication

```bash
$ kubectl exec -it my-vault-0 -- sh

$ vault auth list
Path      Type     Accessor               Description                Version
----      ----     --------               -----------                -------
token/    token    auth_token_e6054326    token based credentials    n/a

$ vault auth enable kubernetes
Success! Enabled kubernetes auth method at: kubernetes/

$ vault auth list
Path           Type          Accessor                    Description                Version
----           ----          --------                    -----------                -------
kubernetes/    kubernetes    auth_kubernetes_e0d795ef    n/a                        n/a
token/         token         auth_token_e6054326         token based credentials    n/a

# Configure the Kubernetes authentication method to use the location of the Kubernetes API.
# KUBERNETES_PORT_443_TCP_ADDR is the IP of the 'kubernetes' ClusterIp service 
$ echo $KUBERNETES_PORT_443_TCP_ADDR

$ vault write auth/kubernetes/config kubernetes_host="https://$KUBERNETES_PORT_443_TCP_ADDR:443"
$ vault read auth/kubernetes/config

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
```

### Create a policy, that enabled `read` capability for secrets at `secret/data/vault-agent-sidecar-injector-demo`

```bash
$ vault policy write vault-agent-sidecar-injector-demo-policy - <<EOF
path "secret/data/vault-agent-sidecar-injector-demo" {
   capabilities = ["read"]
}
EOF
Success! Uploaded policy: vault-agent-sidecar-injector-demo-policy
$ vault policy list
$ vault policy read vault-agent-sidecar-injector-demo-policy
```

### Define a Kubernetes service account

```bash
$ kubectl create sa vault-agent-sidecar-injector-demo-sa
$ kubectl get sa

default                                0         68m
my-postgresql                          0         68m
my-vault-agent-injector                0         41m
my-vault                               0         41m
vault-agent-sidecar-injector-demo-sa   0         23s
```

### Create a Kubernetes authentication role

The role connects the Kubernetes service account `vault-agent-sidecar-injector-demo-sa` and namespace `default`
with the Vault policy, `vault-agent-sidecar-injector-demo-policy`.
The tokens returned after authentication are valid for 24 hours.
The secret is bound to the `vault-agent-sidecar-injector-demo-sa` service account and to the `default` namespace

```bash
$ vault write auth/kubernetes/role/vault-agent-sidecar-injector-demo-role \
      bound_service_account_names=vault-agent-sidecar-injector-demo-sa \
      bound_service_account_namespaces=default \
      policies=vault-agent-sidecar-injector-demo-policy \
      ttl=24h 
Success! Data written to: auth/kubernetes/role/vault-agent-sidecar-injector-demo-role     
$ vault read auth/kubernetes/role/vault-agent-sidecar-injector-demo-role

Key                                         Value
---                                         -----
alias_name_source                           serviceaccount_uid
bound_service_account_names                 [vault-agent-sidecar-injector-demo-sa]
bound_service_account_namespace_selector    n/a
bound_service_account_namespaces            [default]
policies                                    [vault-agent-sidecar-injector-demo-policy]
token_bound_cidrs                           []
token_explicit_max_ttl                      0s
token_max_ttl                               0s
token_no_default_policy                     false
token_num_uses                              0
token_period                                0s
token_policies                              [vault-agent-sidecar-injector-demo-policy]
token_ttl                                   24h
token_type                                  default
ttl                                         24h
```


### Inject secrets into the pod (delete the previous one app)

```bash
$ kubectl apply -f k8s/k8s-inject-secrets.yaml
$ kubectl get pods | grep vault-agent-sidecar-injector-demo
vault-agent-sidecar-injector-demo-587b88776c-f9vnv   2/2     Running   0          81s
# display the logs in the vault-agent container, the vault-agent manages the token lifecycle and the secret retrieval.
$ kubectl logs $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") --container vault-agent
==> Vault Agent started! Log data will stream in below:

==> Vault Agent configuration:

           Api Address 1: http://bufconn
                     Cgo: disabled
               Log Level: info
                 Version: Vault v1.21.2, built 2026-01-06T08:33:05Z
             Version Sha: 781ba452d731fe2d59ccbc1b37ca7c5a18edb998

2026-02-23T20:09:33.699Z [INFO]  agent.sink.file: creating file sink
2026-02-23T20:09:33.699Z [INFO]  agent.sink.file: file sink configured: path=/home/vault/.vault-token mode=-rw-r----- owner=100 group=1000
2026-02-23T20:09:33.699Z [INFO]  agent.exec.server: starting exec server
2026-02-23T20:09:33.699Z [INFO]  agent.exec.server: no env templates or exec config, exiting
2026-02-23T20:09:33.699Z [INFO]  agent.auth.handler: starting auth handler
2026-02-23T20:09:33.699Z [INFO]  agent.auth.handler: authenticating
2026-02-23T20:09:33.699Z [INFO]  agent.sink.server: starting sink server
2026-02-23T20:09:33.699Z [INFO]  agent.template.server: starting template server
2026-02-23T20:09:33.699Z [INFO]  agent: (runner) creating new runner (dry: false, once: false)
2026-02-23T20:09:33.699Z [INFO]  agent: (runner) creating watcher
2026-02-23T20:09:33.701Z [INFO]  agent.auth.handler: authentication successful, sending token to sinks
2026-02-23T20:09:33.701Z [INFO]  agent.auth.handler: starting renewal process
2026-02-23T20:09:33.701Z [INFO]  agent.sink.file: token written: path=/home/vault/.vault-token
2026-02-23T20:09:33.701Z [INFO]  agent.template.server: template server received new token
2026-02-23T20:09:33.701Z [INFO]  agent: (runner) stopping
2026-02-23T20:09:33.701Z [INFO]  agent: (runner) creating new runner (dry: false, once: false)
2026-02-23T20:09:33.701Z [INFO]  agent: (runner) creating watcher
2026-02-23T20:09:33.701Z [INFO]  agent: (runner) starting
2026-02-23T20:09:33.701Z [INFO]  agent.auth.handler: renewed auth toke
```

The `vault-agent` was authenticated with the provided service account, got a token and stored it in `/home/vault/.vault-token` folder.
This token is used to request secrets.

```bash
$ cat /home/vault/.vault-token
```

### Display the secret written to the vault-agent-sidecar-injector-demo container.

```bash
$ kubectl exec $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") -c vault-agent -- cat /vault/secrets/database.properties 
data: map[db_password:secret db_url:r2dbc:postgresql://my-postgresql:5432/postgres db_username:postgres]
metadata: map[created_time:2026-02-23T20:00:44.495404094Z custom_metadata:<nil> deletion_time: destroyed:false version:1]      
```

### Apply a template to the injected secrets (remove the previously deployed)

Here we disabled also the environment variable SPRING_R2DBC_URL, the configuration is from
`/vault/secrets/database.properties`

```bash
# delete the previous app
$ kubectl delete -f k8s/k8s-inject-secrets.yaml 
$ kubectl apply -f k8s/k8s-inject-secrets-as-template.yaml
$ kubectl exec $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") -c app -- cat /vault/secrets/database.properties
spring.r2dbc.username=postgres
spring.r2dbc.password=secret
spring.r2dbc.url=r2dbc:postgresql://my-postgresql:5432/postgres  
```

```bash
http :8080/customers\?lastName=Doe
```

## Authentication methods:

Before a client can interact with Vault, it must authenticate against an `auth method` to acquire a token.
This token has policies granting you permission to perform the appropriate operations.

Auth methods:
- Token https://developer.hashicorp.com/vault/docs/auth/token (automatically enabled)
- Kubernetes https://developer.hashicorp.com/vault/docs/auth/kubernetes
- AppRole https://developer.hashicorp.com/vault/docs/auth/approle (recommended for machines or apps)
- Userpass https://developer.hashicorp.com/vault/docs/auth/userpass
- LDAP https://developer.hashicorp.com/vault/docs/auth/ldap (user authentication against an existing LDAP server)

```bash
$ vault auth list
```

## Vault - Kubernetes authentication

https://developer.hashicorp.com/vault/docs/auth/kubernetes

The kubernetes auth method can be used to authenticate with `Vault` using a `Kubernetes Service Account Token`.  
This token is provided to each pod when it is created.
- The vault secrets are bound to the service account.
- The vault secrets are bound to the namespace

Kubernetes Service Account Token

```bash
$ kubectl get sa
NAME                                   SECRETS   AGE
default                                0         59m
my-postgresql                          0         58m
my-vault                               0         51m
my-vault-agent-injector                0         51m
vault-agent-sidecar-injector-demo-sa   0         39m

$ kubectl create token vault-agent-sidecar-injector-demo-sa
eyJhbGciOiJSUzI1NiIsImtpZCI6ImVWTW9Fd1E0VHlWaHV2Z0lzMFhYRmdhUEJzeVFpa0Z0RVdHUUJuOVVQQU0ifQ.e...
```

## Vault - AppRole authentication (TODO)

- `AppRole` is an authentication mechanism within Vault to allow machines or apps to acquire a token to interact with Vault. 
- It uses `RoleID` and `SecretID` for login.
- The `RoleID` is equivalent to a username, and `SecretID` is the corresponding password
- If the `SecretID` used for login is fetched from an `AppRole`, this is operating in `Pull` mode
- If a "custom" `SecretID` is set against an `AppRole` by the client, it is referred to as a `Push` mode - deprecated

### RoleID delivery best practices https://developer.hashicorp.com/vault/docs/auth/approle/approle-pattern#roleid-delivery-best-practices

- `RoleID` is an identifier that selects the `AppRole` against which the other credentials are evaluated.
- Generally, you create a role per application to ensure that each application will have a unique `RoleID`.
- You can embed the `RoleID` value into a machine image or container as a text file or environment variable.

### SecretID delivery best practices https://developer.hashicorp.com/vault/docs/auth/approle/approle-pattern#secretid-delivery-best-practices



### Enable AppRole auth method - (Persona: app)

```bash
$ kubectl exec -it my-vault-0 -- sh
$ vault auth enable approle
Success! Enabled approle auth method at: approle/
```

### Create a role with policy attached - (Persona: app)

When you enabled the `AppRole` auth method, it gets mounted at the `/auth/approle` path.

```bash
# Before creating a role, create a `gitlab` policy.
$ vault policy write gitlab -<<EOF
# Read-only permission on secrets stored at 'secret/data/mysql/webapp'
path "secret/data/mysql/webapp" {
  capabilities = [ "read" ]
}
EOF
# Success! Uploaded policy: gitlab

# Creates a role named `gitlab` with `gitlab` policy attached.
# The generated token's time-to-live (TTL) is set to 1 hour and can be renewed for up to 4 hours of its first creation.
$ vault write auth/approle/role/gitlab token_policies="gitlab" \
    token_ttl=1h token_max_ttl=4h
# Success! Data written to: auth/approle/role/gitlab
# There are a number of parameters that you can set on a role.
$ vault read auth/approle/role/gitlab
Key                        Value
---                        -----
bind_secret_id             true
local_secret_ids           false
secret_id_bound_cidrs      <nil>
secret_id_num_uses         0
secret_id_ttl              0s
token_bound_cidrs          []
token_explicit_max_ttl     0s
token_max_ttl              4h
token_no_default_policy    false
token_num_uses             0
token_period               0s
token_policies             [gitlab]
token_ttl                  1h
token_type                 default    
```

### Get RoleID and SecretID - (Persona: admin)

```bash
$ vault read auth/approle/role/gitlab/role-id
Key        Value
---        -----
role_id    ab429a9c-0fc3-1a22-e25d-e22a073bc895

# The -force (or -f) flag forces the write operation to continue without any data values specified.
$ vault write -force auth/approle/role/gitlab/secret-id
Key                   Value
---                   -----
secret_id             7574e88e-e865-95b5-9493-9aace9b66cee
secret_id_accessor    a0b90da1-e69e-6fbe-5ff6-b8a1deb4f337
secret_id_num_uses    0
secret_id_ttl         0s
```

### Login with RoleID & SecretID - (Persona: app)

The client (in this case, gitlab) uses the `RoleID` and `SecretID` passed by the admin to authenticate with Vault.

```bash
$ vault write auth/approle/login role_id="ab429a9c-0fc3-1a22-e25d-e22a073bc895" \
    secret_id="7574e88e-e865-95b5-9493-9aace9b66cee"

# Vault returns a client token with "default" and "gitlab" policies attached.    
Key                     Value
---                     -----
token                   <TOKEN>
token_accessor          <TOKEN_ACCESSOR>
token_duration          1h
token_renewable         true
token_policies          ["default" "gitlab"]
identity_policies       []
policies                ["default" "gitlab"]
token_meta_role_name    gitlab

$ vault token lookup  <TOKEN>

Key                 Value
---                 -----
accessor            <TOKEN_ACCESSOR>
creation_time       1738423723
creation_ttl        1h
display_name        approle
entity_id           8b06c4e3-bb3b-7fad-e44d-33fa3c2d26fb
expire_time         2025-02-01T16:28:43.125065301Z
explicit_max_ttl    0s
id                  <TOKEN>
issue_time          2025-02-01T15:28:43.125068375Z
meta                map[role_name:gitlab]
num_uses            0
orphan              true
path                auth/approle/login
policies            [default gitlab]
renewable           true
ttl                 53m38s
type                service    
```

### Read secrets using the AppRole token - (Persona: app)

Once receiving a token from Vault, the client can make future requests using this token.

```bash
$ export APP_TOKEN="<TOKEN>"

$ VAULT_TOKEN=$APP_TOKEN vault kv get secret/mysql/webapp

====== Secret Path ======
secret/data/mysql/webapp

======= Metadata =======
Key                Value
---                -----
created_time       2025-02-01T15:31:45.326139095Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

=== Data ===
Key    Value
---    -----
foo    bar

# The app has a read-only access; therefore, the following delete command will fail.

$ VAULT_TOKEN=$APP_TOKEN vault kv delete secret/mysql/webapp
Error deleting secret/data/mysql/webapp: Error making API request.

URL: DELETE http://127.0.0.1:8200/v1/secret/data/mysql/webapp
Code: 403. Errors:

* 1 error occurred:
	* permission denied

```

### Response wrap the SecretID https://developer.hashicorp.com/vault/tutorials/auth-methods/approle#response-wrap-the-secretid

To keep the `SecretID` confidential, use response wrapping so that only the expecting client can unwrap the SecretID.

```bash
# instead of vault write -force auth/approle/role/gitlab/secret-id
$ vault write -wrap-ttl=60s -force auth/approle/role/gitlab/secret-id
Key                              Value
---                              -----
wrapping_token:                  <TOKEN>
wrapping_accessor:               qZPGx3BywZ69ztqgsI2u9TqB
wrapping_token_ttl:              1m
wrapping_token_creation_time:    2025-02-01 15:49:03.836173638 +0000 UTC
wrapping_token_creation_path:    auth/approle/role/gitlab/secret-id
wrapped_accessor:                0f6c7b85-e2c0-ba52-c6b2-67bdf7d5dd62

$ VAULT_TOKEN="<TOKEN>"
Key                   Value
---                   -----
secret_id             ffbdf459-1ae1-e521-6c1f-0ef487166012
secret_id_accessor    0f6c7b85-e2c0-ba52-c6b2-67bdf7d5dd62
secret_id_num_uses    0
secret_id_ttl         0s

After 60 seconds we get:
$ VAULT_TOKEN="<TOKEN>" vault unwrap

Error unwrapping: Error making API request.

URL: PUT http://127.0.0.1:8200/v1/sys/wrapping/unwrap
Code: 400. Errors:

* wrapping token is not valid or does not exist
```

### Limit the SecretID usages https://developer.hashicorp.com/vault/tutorials/auth-methods/approle#limit-the-secretid-usages

Treat the SecretID like a password and force it to be regenerated after a number of use.

In this example, a `SecretID` of the gitlab role can be used for up to 2 times to authenticate and fetch a client token.

```bash
$ vault write auth/approle/role/gitlab token_policies="jenkins" \
     token_ttl=1h token_max_ttl=4h \
     secret_id_num_uses=2
     
$ vault write -force auth/approle/role/gitlab/secret-id
Key                   Value
---                   -----
secret_id             b17d7696-41c3-1e7f-38e6-b560b53843f2
secret_id_accessor    c7e6f192-a357-872d-a3e8-a656bd19ba16
secret_id_num_uses    2
secret_id_ttl         0s

$ vault write auth/approle/login role_id="ab429a9c-0fc3-1a22-e25d-e22a073bc895" \
    secret_id="b17d7696-41c3-1e7f-38e6-b560b53843f2"

Key                     Value
---                     -----
token                   <TOKEN>
token_accessor          bTOxJeInL0s2jvDkBJTXwuhm
token_duration          1h
token_renewable         true
token_policies          ["default" "jenkins"]
identity_policies       []
policies                ["default" "jenkins"]
token_meta_role_name    gitlab

$ vault write auth/approle/login role_id="ab429a9c-0fc3-1a22-e25d-e22a073bc895" \
    secret_id="b17d7696-41c3-1e7f-38e6-b560b53843f2"

Key                     Value
---                     -----
token                   <TOKEN>
token_accessor          AdN3ht8SDTSBz6dZgNllwBrt
token_duration          1h
token_renewable         true
token_policies          ["default" "jenkins"]
identity_policies       []
policies                ["default" "jenkins"]
token_meta_role_name    gitlab
  
$ vault write auth/approle/login role_id="ab429a9c-0fc3-1a22-e25d-e22a073bc895" \
    secret_id="b17d7696-41c3-1e7f-38e6-b560b53843f2"
    
Error writing data to auth/approle/login: Error making API request.

URL: PUT http://127.0.0.1:8200/v1/auth/approle/login
Code: 400. Errors:

* invalid role or secret ID        
```

### Check client token validity: 

The below token was create with `token_ttl=1h` and is currently valid for `22m52s` (see `ttl`)
```bash
$ vault token lookup <TOKEN>
Key                 Value
---                 -----
accessor            EGpFN3doXRSWKjbaASogz4VL
creation_time       1738423723
creation_ttl        1h
display_name        approle
entity_id           8b06c4e3-bb3b-7fad-e44d-33fa3c2d26fb
expire_time         2025-02-01T16:28:43.125065301Z
explicit_max_ttl    0s
id                  <TOKEN>
issue_time          2025-02-01T15:28:43.125068375Z
meta                map[role_name:gitlab]
num_uses            0
orphan              true
path                auth/approle/login
policies            [default gitlab]
renewable           true
ttl                 22m52s
type                service
```

### Example:




## Resources:

https://developer.hashicorp.com/vault/tutorials/kubernetes/kubernetes-sidecar
https://www.hashicorp.com/blog/refresh-secrets-for-kubernetes-applications-with-vault-agent
https://developer.hashicorp.com/vault/docs/platform/k8s/injector

