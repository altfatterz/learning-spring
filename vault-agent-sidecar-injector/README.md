## Try locally

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

### Build a docker image of the application

```bash
$ mvn spring-boot:build-image
```

### Import the image into the `k8s-cluster` cluster

```bash
$ k3d images import vault-agent-sidecar-injector:0.0.1-SNAPSHOT -c k8s-cluster 
$ docker exec k3d-k8s-cluster-server-0 crictl images | grep vault

docker.io/library/vault-agent-sidecar-injector   0.0.1-SNAPSHOT         baebde64ddd1e       364MB
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
my-vault-internal                   ClusterIP   None            <none>        8200/TCP,8201/TCP   117s
my-vault                            ClusterIP   10.43.69.16     <none>        8200/TCP,8201/TCP   117s
my-vault-agent-injector-svc         ClusterIP   10.43.209.49    <none>        443/TCP             117s

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
# with secrets with field names containing '-' had issues with agent-inject-template. more details: https://support.hashicorp.com/hc/en-us/articles/15667022913299-Vault-Agent-throws-bad-character-U-002D
$ vault kv put -mount=secret vault-agent-sidecar-injector-demo db_username=postgres db_password=secret db_url=r2dbc:postgresql://my-postgresql:5432/postgres
$ vault kv get -mount=secret vault-agent-sidecar-injector-demo

================ Secret Path ================
secret/data/vault-agent-sidecar-injector-demo

======= Metadata =======
Key                Value
---                -----
created_time       2024-09-18T09:04:48.281025014Z
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

You can get this information also with curl like:

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

Vault Agent started! Log data will stream in below:

2024-09-18T09:39:51.090Z [INFO]  agent.sink.file: creating file sink
2024-09-18T09:39:51.090Z [INFO]  agent.sink.file: file sink configured: path=/home/vault/.vault-token mode=-rw-r-----
2024-09-18T09:39:51.091Z [INFO]  agent.exec.server: starting exec server
2024-09-18T09:39:51.091Z [INFO]  agent.exec.server: no env templates or exec config, exiting
2024-09-18T09:39:51.091Z [INFO]  agent.sink.server: starting sink server
2024-09-18T09:39:51.091Z [INFO]  agent.template.server: starting template server
2024-09-18T09:39:51.091Z [INFO]  agent.auth.handler: starting auth handler
2024-09-18T09:39:51.091Z [INFO]  agent.auth.handler: authenticating
2024-09-18T09:39:51.091Z [INFO]  agent: (runner) creating new runner (dry: false, once: false)
2024-09-18T09:39:51.092Z [INFO]  agent: (runner) creating watcher
==> Vault Agent configuration:

           Api Address 1: http://bufconn
                     Cgo: disabled
               Log Level: info
                 Version: Vault v1.17.2, built 2024-07-05T15:19:12Z
             Version Sha: 2af5655e364f697a15b1dc2db2c3f85f6ef949f2

2024-09-18T09:39:51.098Z [INFO]  agent.auth.handler: authentication successful, sending token to sinks
2024-09-18T09:39:51.098Z [INFO]  agent.auth.handler: starting renewal process
2024-09-18T09:39:51.098Z [INFO]  agent.template.server: template server received new token
2024-09-18T09:39:51.098Z [INFO]  agent: (runner) stopping
2024-09-18T09:39:51.098Z [INFO]  agent: (runner) creating new runner (dry: false, once: false)
2024-09-18T09:39:51.098Z [INFO]  agent: (runner) creating watcher
2024-09-18T09:39:51.098Z [INFO]  agent: (runner) starting
2024-09-18T09:39:51.098Z [INFO]  agent.sink.file: token written: path=/home/vault/.vault-token
2024-09-18T09:39:51.101Z [INFO]  agent.auth.handler: renewed auth token
```

The `vault-agent` was authenticated with the provided service account, got a token and stored it in `/home/vault/.vault-token` folder.
This token is used to request secrets.

```bash
$ cat /home/vault/.vault-token
hvs.CAESIKuU2F2OQOum8ueIBsnA8TBgDR_tgNoiJ3OyzG31rjjAGh4KHGh2cy5vaEtHd0RsOVVVOGlwaDZLbHlRUzZQTUE
```

### Display the secret written to the vault-agent-sidecar-injector-demo container.

```bash
$ kubectl exec $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") -c app -- cat /vault/secrets/database.properties 
data: map[spring.r2dbc.password:secret spring.r2dbc.url:r2dbc:postgresql://my-postgresql:5432/postgres spring.r2dbc.username:postgres]
metadata: map[created_time:2024-09-18T09:04:48.281025014Z custom_metadata:<nil> deletion_time: destroyed:false version:3]      
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

## TODO: Vault Agent using AppRole authentication

### Resources:

https://developer.hashicorp.com/vault/tutorials/kubernetes/kubernetes-sidecar#role
https://www.hashicorp.com/blog/refresh-secrets-for-kubernetes-applications-with-vault-agent
https://developer.hashicorp.com/vault/docs/platform/k8s/injector

