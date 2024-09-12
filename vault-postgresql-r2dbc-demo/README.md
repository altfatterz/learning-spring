### Vault with PostgreSQL Demo

#### Start PostgreSQL and Vault instance

```bash
$ cd vault-postgresql-r2dbc-demo
$ docker compose up -d
```

### Store database connection configuration in Vault

```bash
$ docker exec -it vault sh
$ vault --version
Vault v1.17.5 (4d0c53e84094b8017d32b6e5b7f8142035c8837f), built 2024-08-30T15:54:57Z
```

```bash
// default is https://127.0.0.1:8200
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"
```

### Store the secret

```bash
$ vault kv put -mount=secret vault-postgresql-r2dbc-demo db.username=postgres db.password=secret db.url=r2dbc:postgresql://db:5432/postgres
$ vault kv get -mount=secret vault-postgresql-r2dbc-demo
# to delete if needed
$ vault kv delete -mount=secret vault-postgresql-r2dbc-demo 
```

### Check the stored secrets in Vault UI

http://localhost:8200

### Build the image

```bash
$ mvn spring-boot:build-image
```

### Run the application

```bash
$ docker run --network vault-postgresql-r2dbc-demo_default docker.io/library/vault-postgresql-r2dbc-demo:0.0.1-SNAPSHOT
```

### Check the database:

```bash
$ psql -h localhost -p <port> -U postgres
\d
$ select * from customers;
```

### Access the API

```bash
$ http :8080/customers\?lastName=Doe
HTTP/1.1 200 OK
Content-Type: application/json
transfer-encoding: chunked

[
    {
        "id": 10,
        "name": "John Doe"
    },
    {
        "id": 11,
        "name": "Jane Doe"
    }
]
```