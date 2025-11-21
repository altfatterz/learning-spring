### Vault Demo

#### Start a Vault instance

```bash
$ cd vault-demo
$ docker compose up -d
```

### Store configuration in Vault

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

```bash
// this is the deprecated format
$ vault kv put secret/vault-demo example.username=demouser example.password=demopassword
or 
$ vault kv put -mount=secret vault-demo example.username=demouser example.password=demopassword
$ vault kv get -mount=secret vault-demo

$ vault kv put -mount=secret vault-demo/cloud example.username=clouduser example.password=cloudpassword
$ vault kv get -mount=secret vault-demo/cloud
```

### Run the application

```bash
$ mvn spring-boot:run

2024-09-09T10:14:50.190+02:00  INFO 33613 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : ----------------------------------------
2024-09-09T10:14:50.190+02:00  INFO 33613 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : Configuration properties
2024-09-09T10:14:50.190+02:00  INFO 33613 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     :    example.username is demouser
2024-09-09T10:14:50.191+02:00  INFO 33613 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     :    example.password is demopassword
2024-09-09T10:14:50.191+02:00  INFO 33613 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : ----------------------------------------

$ mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=cloud"

2024-09-09T10:16:15.352+02:00  INFO 33697 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : ----------------------------------------
2024-09-09T10:16:15.352+02:00  INFO 33697 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : Configuration properties
2024-09-09T10:16:15.352+02:00  INFO 33697 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     :    example.username is clouduser
2024-09-09T10:16:15.362+02:00  INFO 33697 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     :    example.password is cloudpassword
2024-09-09T10:16:15.362+02:00  INFO 33697 --- [vault-demo] [           main] c.g.a.vaultdemo.VaultDemoApplication     : ----------------------------------------

$ mvn clean package
$ java -jar target/vault-demo-0.0.1-SNAPSHOT.jar

$ mvn spring-boot:build-image
// The --network flag tells Docker to attach our 'vault' container to the existing network that our external `vault' container is using
$ docker run --network container:vault  docker.io/library/vault-demo:0.0.1-SNAPSHOT

$ mvn -Pnative spring-boot:build-image
$ docker run --network container:vault  docker.io/library/vault-demo:0.0.1-SNAPSHOT
```

