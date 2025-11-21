### Spring Cloud Config Server Demo

1. Start Vault using `compose.yaml` file

```bash
$ docker compose up -d
```

2. Store secret in Vault

```bash
$ docker exec -it vault sh
$ vault --version
Vault v1.17.5 (4d0c53e84094b8017d32b6e5b7f8142035c8837f), built 2024-08-30T15:54:57Z
// default is https://127.0.0.1:8200
export VAULT_ADDR="http://127.0.0.1:8200"
export VAULT_TOKEN="00000000-0000-0000-0000-000000000000"

$ vault status
Key             Value
---             -----
Seal Type       shamir
Initialized     true
Sealed          false
Total Shares    1
Threshold       1
Version         1.17.5
Build Date      2024-08-30T15:54:57Z
Storage Type    inmem
Cluster Name    vault-cluster-b4b4e071
Cluster ID      4735af40-3d28-6d74-0379-083a81bb45f0
HA Enabled      false

// list the secret engines
$ vault secrets list
Path          Type         Accessor              Description
----          ----         --------              -----------
cubbyhole/    cubbyhole    cubbyhole_a3592333    per-token private secret storage
identity/     identity     identity_b1073000     identity store
secret/       kv           kv_99ab2fdb           key/value secret storage
sys/          system       system_df236720       system endpoints used for control, policy and debugging

$ vault kv put -mount=secret config-server-client/dev message="Hello Dev from Vault!"
$ vault kv put -mount=secret config-server-client/prd message="Hello Prod from Vault!"
$ vault kv get -mount=secret config-server-client/dev
============ Secret Path ============
secret/data/config-server-client/dev

======= Metadata =======
...
===== Data =====
Key        Value
---        -----
message    Hello Dev from Vault!
$ vault kv get -mount=secret config-server-client/prd
```

```bash
http :8200/v1/secret/data/config-server-client/dev "X-Vault-Token: 00000000-0000-0000-0000-000000000000"
{
    "auth": null,
    "data": {
        "data": {
            "message": "Hello Dev from Vault!"
        },
        "metadata": {
            "created_time": "2024-09-20T08:07:43.993471087Z",
            "custom_metadata": null,
            "deletion_time": "",
            "destroyed": false,
            "version": 1
        }
    },
    "lease_duration": 0,
    "lease_id": "",
    "mount_type": "kv",
    "renewable": false,
    "request_id": "495691bd-cbf3-8e49-d613-220448674255",
    "warnings": null,
    "wrap_info": null
}
```

3. Start `ConfigServerVaultBackendApplication` in IntelliJ

4. Verify that client configuration is exposed:

```bash
$ http :8888/config-server-client/dev
$ http :8888/config-server-client/prd 
```

3. Start the client in `dev` profile

```bash
$ mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

4. Verify that the configuration is taken from config server

```bash
$ http :8080/message
Hello Dev from Vault!
```

5. Modify the secret in Vault and verify the config server knows about the change:


```bash
$ vault kv put -mount=secret config-server-client/dev message="Hello Dev from Vault! - changed"
$ http :8888/config-server-client/dev
{
...
    "propertySources": [
        {
            "name": "vault:config-server-client/dev",
            "source": {
                "message": "Hello Dev from Vault! - changed"
            }
        }
    ],
...
}
```

6. Verify that the client does yet know about the change:

```bash
$ http :8080/message
Hello Dev from Vault!
```

7. Send a refresh to the client

```bash
$ echo {} | http post :8080/actuator/refresh
```

In the logs you should see:

```bash
2024-09-20T10:54:35.012+02:00  INFO 39216 --- [config-server-client] [nio-8080-exec-1] o.s.c.c.c.ConfigServerConfigDataLoader   : Fetching config from server at : http://localhost:8888
2024-09-20T10:54:35.024+02:00  INFO 39216 --- [config-server-client] [nio-8080-exec-1] o.s.c.c.c.ConfigServerConfigDataLoader   : Located environment: name=config-server-client, profiles=[dev], label=null, version=null, state=null
2024-09-20T10:54:35.177+02:00  INFO 39216 --- [config-server-client] [nio-8080-exec-1] o.s.cloud.endpoint.RefreshEndpoint       : Refreshed keys : [message]
```

8. Verify that the client knows about the change:

```bash
$ http :8080/message
Hello Dev with 'file:' prefix! - changed
```
