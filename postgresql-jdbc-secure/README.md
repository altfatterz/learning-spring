### Secure TCP/IP Connections with SSL:

#### Start up PostgreSQL

1. Create the `postgres-certs` folder using the `create-certs.sh`

```bash
./create-certs.sh
```

2. Start up PostgreSQL

```bash
$ docker compose up
```

In the logs you should see:

db-1  | /usr/local/bin/docker-entrypoint.sh: running /docker-entrypoint-initdb.d/config.sql
db-1  | ALTER SYSTEM
db-1  | ALTER SYSTEM
db-1  | ALTER SYSTEM

#### Test SSL connectivity:

Note: PostgreSQL does not begin the SSL negotiation until you've sent a packet instructing it to do so. s_client expects it to do SSL before that packet.

```bash
$ echo "" | openssl s_client -starttls postgres -connect localhost:5432 -showcerts

Connecting to 127.0.0.1
CONNECTED(00000005)
Can't use SSL_get_servername
depth=0 CN=localhost
verify error:num=18:self-signed certificate
verify return:1
depth=0 CN=localhost
verify return:1
---
Certificate chain
 0 s:CN=localhost
   i:CN=localhost
   a:PKEY: rsaEncryption, 2048 (bit); sigalg: RSA-SHA256
   v:NotBefore: Sep 25 08:16:57 2024 GMT; NotAfter: Oct 25 08:16:57 2024 GMT
-----BEGIN CERTIFICATE-----
MIIDCTCCAfGgAwIBAgIUV+rh5akKWFiwzGhM1yfWBgLw93UwDQYJKoZIhvcNAQEL
BQAwFDESMBAGA1UEAwwJbG9jYWxob3N0MB4XDTI0MDkyNTA4MTY1N1oXDTI0MTAy
NTA4MTY1N1owFDESMBAGA1UEAwwJbG9jYWxob3N0MIIBIjANBgkqhkiG9w0BAQEF
AAOCAQ8AMIIBCgKCAQEAvKCKCQ7dPGmUxpjdsWsjEtBXVSB7SQSpG1lNtHtElk8N
R4VoVBzb4sxqEOWNm1vaMZV4pret4Ls1OPp3wGm5/25iLYJA6U5542lKEfLHnu7L
AGbi9i+mh2itN4QyPTg0+rOBykx5F27haI7UddRGADK3syO0RwdQhvWF/X73rzCn
IGfcpeHmC52tBqkuy9OqaYX1t6sZiXjAbQMZF4Y5XX8jqR3LtQZw9KYMNmV5Yte7
kG/5GEEDGTTPuoTJ8jhyxRd68ZRpaSvw8OlwaJ7Q1zcSpqmS+IuLmJPIdw7IP4bN
QM923gMal4ZxtO2x+YtXUKsCgVHkqvlt2dojHqoF4wIDAQABo1MwUTAdBgNVHQ4E
FgQUtAv66+eNsstevxF/5x/ZwM1uvHYwHwYDVR0jBBgwFoAUtAv66+eNsstevxF/
5x/ZwM1uvHYwDwYDVR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEATNXK
qaOBOKTCeD9HCTMF7itYuAXrhZrd1kOkz9PX0dD6LTB0wK5kky/rTW0ZlS/bTHl9
EMK1Zve1aypYMlCYVVp5IE9Ri2o3TEbslzGvDaPAoVdnd2rp3WE1LIst6W6SuC4I
2GsY6veTFogz0VSpCO1xzDeyjWtAdYVmAkH/esHTtgoOdaMSKqL348XheDsBi8uv
RuCOPfjMnHEWRgXvgyABlEsglNcpsLQ7yggqd7heEAsaDHkjQYaC9QT/LvwrQaUs
Zro7ZZOczK4BJeMReQIIDA9+f/HBYjxUW+5PI0BHEmjtiUxms5X8qw6wQZwR5xEx
L0Ghph8woGdAsnKzFg==
-----END CERTIFICATE-----
---
Server certificate
subject=CN=localhost
issuer=CN=localhost
---
No client certificate CA names sent
Peer signing digest: SHA256
Peer signature type: RSA-PSS
Server Temp Key: ECDH, prime256v1, 256 bits
---
SSL handshake has read 1464 bytes and written 719 bytes
Verification error: self-signed certificate
---
New, TLSv1.3, Cipher is TLS_AES_256_GCM_SHA384
Server public key is 2048 bit
This TLS version forbids renegotiation.
Compression: NONE
Expansion: NONE
No ALPN negotiated
Early data was not sent
Verify return code: 18 (self-signed certificate)
---
DONE

```

#### Check also with `psql` client

Install PostgreSQL to have `psql` client:

```bash
$ brew update
$ brew doctor
$ brew install postgresql@16
$ psql --version
$ psql (PostgreSQL) 16.4 (Homebrew)
```

```bash
$ psql -h localhost -U postgres
psql (16.4 (Homebrew), server 16.3 (Debian 16.3-1.pgdg120+1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, compression: off)
postgres=# show ssl;
 ssl
-----
 on
(1 row)
```

We can still connect without SSL enabled, not sure currently how to disable that.

```bash
$ psql sslmode=disable -U postgres -h localhost
psql (16.4 (Homebrew), server 16.3 (Debian 16.3-1.pgdg120+1))
postgres=#
```

To clean out all containers on my development machine:

```bash
$ docker rm -v -f $(docker ps -qa)
```

Resources: 

https://www.postgresql.org/docs/current/ssl-tcp.html
https://jdbc.postgresql.org/documentation/ssl/
https://serverfault.com/questions/233034/ssl-on-postgres

Smallstep: https://smallstep.com/hello-mtls/doc/combined/postgresql/psql