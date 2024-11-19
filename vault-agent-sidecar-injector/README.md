### Try locally

1. Start PostgreSQL

```bash
$ docker compose up -d
```

2. Start the application `DemoApp`

3. Access the endpoint:

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

### Running on Kubernetes

1. Create a Kubernetes cluster:

```bash
$ k3d cluster create my-k8s-cluster
$ kubectl cluster-info
```

2. Install PostgreSQL:

```bash
$ helm repo add bitnami https://charts.bitnami.com/bitnami 
$ helm repo update
$ helm install my-postgresql bitnami/postgresql -f postgresql-values.yaml
$ helm status my-postgresql
```

3. Test connection to PostgreSQL:

```bash
$ kubectl port-forward svc/my-postgresql 5432:5432
$ PGPASSWORD=secret psql -h localhost -U postgres -d postgres -p 5432
```

4. Build a docker image:

```bash
$d
```

5. Import the image and verify

```bash
$ k3d images import vault-agent-sidecar-injector:0.0.1-SNAPSHOT -c my-k8s-cluster 
$ docker exec k3d-my-k8s-cluster-server-0 crictl images | grep vault

docker.io/library/vault-agent-sidecar-injector   0.0.1-SNAPSHOT         9dab214fd7203       361MB
```

6. Install the app

```bash
$ kubectl apply -f k8s/k8s.yaml
```

7. Test it

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

### Using Vault Agent Sidecar Injector

- Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update
# install Vault in development mode
$ helm install my-vault hashicorp/vault --set "server.dev.enabled=true"
$ kubectl get svc | grep my-vault
my-vault-internal                   ClusterIP   None            <none>        8200/TCP,8201/TCP   117s
my-vault                            ClusterIP   10.43.69.16     <none>        8200/TCP,8201/TCP   117s
my-vault-agent-injector-svc         ClusterIP   10.43.209.49    <none>        443/TCP             117s
$ kubectl get pods | grep my-vault
my-vault-agent-injector-6f7bdcdc9-pfjqv              1/1     Running   0          106s
my-vault-0                                           1/1     Running   0          106s
```

- Access the Vault UI

```bash
$ kubectl port-forward svc/my-vault 8200:8200
# token is 'root', see logs: kubectl logs -f my-vault-0 
$ open http://localhost:8200/ui 
```

- Add the databases connection details into Vault

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
version            3

======= Data =======
Key            Value
---            -----
db_password    secret
db_url         r2dbc:postgresql://my-postgresql:5432/postgres
db_username    postgres
```

- Configure Kubernetes authentication

```bash
$ kubectl exec -it my-vault-0 -- sh
$ vault auth enable kubernetes
Success! Enabled kubernetes auth method at: kubernetes/
$ vault auth list
Path           Type          Accessor                    Description                Version
----           ----          --------                    -----------                -------
kubernetes/    kubernetes    auth_kubernetes_5f45404b    n/a                        n/a
token/         token         auth_token_d6d4b08c         token based credentials    n/a
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

- Create a policy, that enabled `read` capability for secrets at `secret/data/vault-agent-sidecar-injector-demo`

```bash
$ vault policy write vault-agent-sidecar-injector-demo-policy - <<EOF
path "secret/data/vault-agent-sidecar-injector-demo" {
   capabilities = ["read"]
}
EOF
$ vault policy list
$ vault policy read vault-agent-sidecar-injector-demo-policy
```

- Create a Kubernetes authentication role

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
$ vault read auth/kubernetes/role/vault-agent-sidecar-injector-demo-role
```

- Define a Kubernetes service account

```bash
$ kubectl create sa vault-agent-sidecar-injector-demo-sa
$ kubectl get sa

default                                0         68m
my-postgresql                          0         68m
my-vault-agent-injector                0         41m
my-vault                               0         41m
vault-agent-sidecar-injector-demo-sa   0         23s
```

- Inject secrets into the pod (delete the previous one app)

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
$ /vault/secrets $ cat /home/vault/.vault-token
hvs.CAESIKTVOxnjfJpN22NTQd14939OL_DSg2tclCwrZil94zAAGh4KHGh2cy5qU1dVWFl6cUoyZ0VCT01xc0YwR1Q3TmY/vault/secrets
```

- Display the secret written to the vault-agent-sidecar-injector-demo container.

```bash
$ kubectl exec $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") --container vault-agent -- cat /vault/secrets/database.properties 
data: map[spring.r2dbc.password:secret spring.r2dbc.url:r2dbc:postgresql://my-postgresql:5432/postgres spring.r2dbc.username:postgres]
metadata: map[created_time:2024-09-18T09:04:48.281025014Z custom_metadata:<nil> deletion_time: destroyed:false version:3]      
```

- Apply a template to the injected secrets

Here we disabled also the environment variable SPRING_R2DBC_URL, the configuration is from
`/vault/secrets/database.properties`

```bash
# delete the previous app
$ kubectl delete -f k8s/k8s-inject-secrets.yaml 
$ kubectl apply -f k8s/k8s-inject-secrets-as-template.yaml
$ kubectl exec $(kubectl get pod -l app=vault-agent-sidecar-injector-demo -o jsonpath="{.items[0].metadata.name}") --container vault-agent -- cat /vault/secrets/database.properties
spring.r2dbc.username=postgres
spring.r2dbc.password=secret
spring.r2dbc.url=r2dbc:postgresql://my-postgresql:5432/postgres  
```

```bash
http :8080/customers\?lastName=Doe
```

### TODO: Vault Agent using AppRole authentication

### Resources:

https://developer.hashicorp.com/vault/tutorials/kubernetes/kubernetes-sidecar#role
https://www.hashicorp.com/blog/refresh-secrets-for-kubernetes-applications-with-vault-agent
https://developer.hashicorp.com/vault/docs/platform/k8s/injector

