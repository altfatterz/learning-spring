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