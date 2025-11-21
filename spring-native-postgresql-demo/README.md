### Spring Native with PostgreSQL Demo

```bash
$ sdk install java 21.0.2-graalce
$ sdk use java 21.0.2-graalce
$ java -version
openjdk version "21.0.2" 2024-01-16
OpenJDK Runtime Environment GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.2+13.1 (build 21.0.2+13-jvmci-23.1-b30, mixed mode, sharing)
```


```bash
$ sdk use java 21.0.2-graalce
$ mvn -Pnative native:compile
```

### Start PostgreSQL with Docker

```bash
$ docker compose -f docker-compose-postgres.yaml up 
```

### Connect to PostgreSQL: 

```bash
$ psql -h localhost -p 52516 -U postgres
```

### Start the app

```bash
$ ./target/spring-native-postgresql-demo \
    --spring.datasource.url=jdbc:postgresql://localhost:57166/postgres \
    --spring.datasource.username=postgres \
    --spring.datasource.password=secret
```

### Check the logs

```bash
Starting AOT-processed SpringNativePostgresqlDemoApplication using Java 17.0.12 with PID 10005
...
Started SpringNativePostgresqlDemoApplication in 0.309 seconds (process running for 0.323)
...
```

### Memory usage:

```bash
$ vmmap 10005 | grep Physical
Physical footprint:         63.4M
Physical footprint (peak):  63.4M
```

### Size on disk

```bash
$ ls -lh target/spring-native-postgresql-demo | awk '{ print $5; }'
89M
```

### Check endpoint

```bash
$ http :8080/customers
```

### Build a container for the app and run it together with Docker Compose

```bash
$ mvn -Pnative clean spring-boot:build-image
$ docker compose -f docker-compose-combined.yaml up -d
```

```bash
$ docker ps 

CONTAINER ID   IMAGE                                          COMMAND                  CREATED          STATUS                        PORTS                     NAMES
0a365e9fdc32   spring-native-postgresql-demo:0.0.1-SNAPSHOT   "/cnb/process/web"       7 seconds ago    Up 4 seconds                  0.0.0.0:8080->8080/tcp    spring-native-postgresql-demo-app-1
fe5bad032834   postgres:16                                    "docker-entrypoint.s…"   7 seconds ago    Up 7 seconds (healthy)        0.0.0.0:52876->5432/tcp   spring-native-postgresql-demo-db-1
```

