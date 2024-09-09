### PostgreSQL R2DBC Demo

Start the `TestPostgresqlR2dbcApplication`

```bash
$ psql -h localhost -p 5432 -d postgres -U postgres
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
        "id": 1,
        "name": "John Doe"
    },
    {
        "id": 2,
        "name": "Jane Doe"
    }
]
```

### Running without Testcontainers

- Start PostgreSQL

```bash
$ docker compose up -d
```

- Start the application

```bash
$ mvn clean package
$ java -jar target/postgresql-r2dbc-0.0.1-SNAPSHOT.jar \
--spring.r2dbc.url=r2dbc:postgresql://localhost:<port>/postgres \
--spring.r2dbc.username=postgres \
--spring.r2dbc.password=secret
```

- Add data to database
```bash
$ psql -p <port> -U postgres
insert into customers(firstname, lastname) values ('John', 'Doe');
insert into customers(firstname, lastname) values ('Jane', 'Doe');
insert into customers(firstname, lastname) values ('John', 'Wick');
```

Resources:

https://gokhana.dev/spring-r2dbc-for-reactive-relational-databases-in-reactive-programming/