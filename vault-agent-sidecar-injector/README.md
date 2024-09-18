### Try locally

1. Start PostgreSQL

```bash
$ docker compose uo -d
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
```

2. Install PostgreSQL:

```bash
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
$ mvn spring-boot:build-image
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
