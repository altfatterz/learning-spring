

### Start up PostgreSQL:

```bash
$ ./create-certs.sh
$ docker compose up -d
```

### Start up `PostgresqlR2dbcSecureApplication`

### Verify

```bash
$ psql -U postgres -h localhost
psql (16.4 (Homebrew), server 16.3 (Debian 16.3-1.pgdg120+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, compression: off)
```

### Verify client

```bash
$ http :8080/customers\?lastName=Doe
```