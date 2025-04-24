### Postgresql R2DBC demo

### Start test

Start the `TestPostgresqlR2dbcApplication` - should start PostgreSQL with TestContainer


```bash
docker ps

CONTAINER ID   IMAGE                       COMMAND                  CREATED          STATUS          PORTS                     NAMES
40efd0b338d0   postgres:16                 "docker-entrypoint.s…"   12 minutes ago   Up 12 minutes   0.0.0.0:60432->5432/tcp   reverent_moore
9a4f021f8467   testcontainers/ryuk:0.7.0   "/bin/ryuk"              12 minutes ago   Up 12 minutes   0.0.0.0:60430->8080/tcp   testcontainers-ryuk-dd3c4df3-46f8-4193-94fa-f8ad909e98db
```

```bash
$ psql -h localhost -p <port> -d postgres -U postgres
secret
test=# \d
            List of relations
 Schema |     Name     |   Type   | Owner
--------+--------------+----------+-------
 public | facts        | table    | admin
 public | facts_id_seq | sequence | admin
```


```bash
$ http :8080/customers\?lastName=Doe

[
    {
        "balance": 123.456789,
        "id": 1,
        "name": "John Doe :123.456789"
    },
    {
        "balance": 1e-06,
        "id": 2,
        "name": "Jane Doe :0.000001"
    }
]
```

### Running without Testcontainers

- Start PostgreSQL

```bash
$ docker compose -f compose-postgresql.yaml up -d
```

- Start the application

```bash
$ mvn clean package
$ java -jar target/postgresql-r2dbc-0.0.1-SNAPSHOT.jar \
--spring.r2dbc.url=r2dbc:postgresql://localhost:5432/postgres \
--spring.r2dbc.username=postgres \
--spring.r2dbc.password=secret
```

- Add data to database
```bash
$ psql -p <port> -U postgres
insert into customers(firstname, lastname, balance) values ('John', 'Doe', 0.01);
insert into customers(firstname, lastname, balance) values ('Jane', 'Doe', 0.002);
insert into customers(firstname, lastname, balance) values ('John', 'Wick', 0.0003);
```

### Running with Native Build

Build:
```bash
$ mvn -Pnative clean native:compile
```

Run:
```bash
export SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/postgres
export SPRING_R2DBC_USERNAME=postgres
export SPRING_R2DBC_PASSWORD=secret 
./target/postgresql-r2dbc

***************************
APPLICATION FAILED TO START
***************************

Description:

Failed to configure a ConnectionFactory: 'url' attribute is not specified and no embedded database could be configured.

Reason: Failed to determine a suitable R2DBC Connection URL
```

### Running with Native Build using Docker

Create jar:

```bash
$ mvn clean install 
```

Build Image:

```bash
$ mvn -Pnative spring-boot:build-image
```

Run:

```bash
$ docker compose -f docker-compose-combined.yaml up -d

***************************
APPLICATION FAILED TO START
***************************

Description:

Failed to configure a ConnectionFactory: 'url' attribute is not specified and no embedded database could be configured.

Reason: Failed to determine a suitable R2DBC Connection URL
```

### Resources:

https://gokhana.dev/spring-r2dbc-for-reactive-relational-databases-in-reactive-programming/