# Spring WebFlux with R2DBC

## Start up PostgreSQL

```bash
$ docker compose up -d
```

## Start up the App using `SpringWebfluxAndR2DBCApp`

## Test the endpoints

```bash
$ http :8080/payments
HTTP/1.1 200 OK
Content-Type: application/json
transfer-encoding: chunked

[
    {
        "amount": 50.65,
        "id": 17,
        "payee": "Doe",
        "payer": "John"
    },
    {
        "amount": 42.15,
        "id": 18,
        "payee": "Doe",
        "payer": "Jane"
    }
]
```

```bash
$ http :8080/payments/14
HTTP/1.1 200 OK
Content-Length: 53
Content-Type: application/json

{
    "amount": 42.15,
    "id": 18,
    "payee": "Doe",
    "payer": "Jane"
}
```