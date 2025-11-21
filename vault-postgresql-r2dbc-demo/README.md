### Vault with PostgreSQL Demo

#### Start PostgreSQL and Vault instance

```bash
$ cd vault-postgresql-r2dbc-demo
$ docker compose up -d
```

```bash
$ docker ps

CONTAINER ID   IMAGE                    COMMAND                  CREATED          STATUS          PORTS                    NAMES
6fc09998ff6f   postgres:16              "docker-entrypoint.s…"   35 minutes ago   Up 35 minutes   0.0.0.0:5432->5432/tcp   vault-postgresql-r2dbc-demo-postgres-1
deb17201a539   hashicorp/vault:1.13.3   "docker-entrypoint.s…"   35 minutes ago   Up 35 minutes   0.0.0.0:8200->8200/tcp   vault
```

### Check the stored secrets in Vault UI

http://localhost:8200

### Run the application

`VaultPostgresqlR2dbcDemoApplication`

### Check the database:

```bash
$ PGPASSWORD=secret psql -h localhost -p 5432 -U postgres
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

### Running as Docker container

1. Build the image

```bash
$ mvn spring-boot:build-image
```

2. Adjust the config:

```yaml
spring:
  cloud:
    vault:
      host: vault
```

3. Adjust db.url in compose file and restart docker compose

```bash
db.url=r2dbc:postgresql://postgres:5432/postgres
```

4. Start with `docker run`

```bash
$ docker run --network vault-postgresql-r2dbc-demo_default -p 8080:8080 docker.io/library/vault-postgresql-r2dbc-demo:0.0.1-SNAPSHOT
```

5. Access the app

```bash
$ http :8080/customers\?lastName=Doe
```

### Access CLI Vault

```bash
$ docker exec -it vault sh
$ vault --version
Vault v1.17.5 (4d0c53e84094b8017d32b6e5b7f8142035c8837f), built 2024-08-30T15:54:57Z
// default is https://127.0.0.1:8200
$ export VAULT_ADDR="http://127.0.0.1:8200"
$ export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"

$ vault kv get -mount=secret vault-postgresql-r2dbc-demo

============= Secret Path =============
secret/data/vault-postgresql-r2dbc-demo

======= Metadata =======
Key                Value
---                -----
created_time       2024-09-16T12:27:28.965815704Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

======= Data =======
Key            Value
---            -----
db.password    secret
db.url         r2dbc:postgresql://postgres:5432/postgres
db.username    postgres
```



### Resources

https://testcontainers.com/guides/simple-local-development-with-testcontainers-desktop/
https://dev.to/breda/dynamic-postgresql-credentials-using-hashicorp-vault-with-php-symfony-go-examples-4imj
https://www.baeldung.com/spring-dynamicpropertysource
https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/

