### Spring Cloud with Vault Backend using Approle authentication

#### Start Vault using `compose.yaml` file

```bash
$ docker compose up -d
```

#### Configure Vault

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

// Enable AppRole auth method
$ vault auth enable approle
Success! Enabled approle auth method at: approle/

$ vault auth list
Path        Type       Accessor                 Description                Version
----        ----       --------                 -----------                -------
approle/    approle    auth_approle_08851426    n/a                        n/a
token/      token      auth_token_c37f21e0      token based credentials    n/a
```

#### Store secret data

```bash
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

#### Create a `config-server-client-policy` policy

```bash
$ vault policy write config-server-client-policy -<<EOF
# Read-only permission on secrets stored at 'secret/data/config-server-client/dev'
path "secret/data/config-server-client/*" {
  capabilities = [ "read" ]
}
path "secret/data/config-server-client" {
  capabilities = [ "read" ]
} 
EOF
Success! Uploaded policy: config-server-client-policy
$ vault policy list
$ vault policy read config-server-client-policy
```

#### Create a `config-server-client-role` role 

Below example says that the generated token's time-to-live (TTL) is set to 2 hour and can be renewed for up to 4 hours of its first creation.
This example creates a role which operates in pull mode.

```bash
$ vault write auth/approle/role/config-server-client-role token_policies="config-server-client-policy" \
    token_ttl=2h token_max_ttl=4h
Success! Data written to: auth/approle/role/config-server-client-role
$ vault list auth/approle/role
config-server-client-role
$ vault read auth/approle/role/config-server-client-role
Key                        Value
---                        -----
bind_secret_id             true
local_secret_ids           false
secret_id_bound_cidrs      <nil>
secret_id_num_uses         0
secret_id_ttl              0s
token_bound_cidrs          []
token_explicit_max_ttl     0s
token_max_ttl              4h
token_no_default_policy    false
token_num_uses             0
token_period               0s
token_policies             [config-server-client-policy]
token_ttl                  2h
token_type                 default
```

#### Get RoleId and SecretId

```bash
$ vault read auth/approle/role/config-server-client-role/role-id
Key        Value
---        -----
role_id    d5a83454-9ddb-7f5e-82ae-71f8a91c3522

# generate a new SecretID
$ vault write -force auth/approle/role/config-server-client-role/secret-id
Key                   Value
---                   -----
secret_id             fb241add-01eb-c2f5-921a-b14aecc7b158
secret_id_accessor    ed656def-3b46-a566-b8a3-57c0566aff2a
secret_id_num_uses    0
secret_id_ttl         0s
```

#### Login with RoleId and SecretId

Get a new shell

```bash
$ docker exec -it vault sh
$ export VAULT_ADDR="http://127.0.0.1:8200"
$ vault kv get secret/config-server-client/dev
URL: GET http://127.0.0.1:8200/v1/sys/internal/ui/mounts/secret/data/config-server-client/dev
Code: 403. Errors: permission denied
// login 
$ vault write auth/approle/login role_id="e94f7a87-3f6f-dfca-7d29-9cdd08abfd09" \
    secret_id="7ef8b034-7e7c-d826-9b98-f469b0cd082d"
Key                     Value
---                     -----
token                   <TOKEN>
token_accessor          <TOKEN_ACCESSOR>
token_duration          2h
token_renewable         true
token_policies          ["config-server-client-policy" "default"]
identity_policies       []
policies                ["config-server-client-policy" "default"]
token_meta_role_name    config-server-client-role

// store the token
export APP_TOKEN="<TOKEN>"
    
// read secrets using the AppRole token
VAULT_TOKEN=$APP_TOKEN vault kv get secret/config-server-client/dev
============ Secret Path ============
secret/data/config-server-client/dev

======= Metadata =======
Key                Value
---                -----
created_time       2024-10-11T07:30:00.968061206Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

===== Data =====
Key        Value
---        -----
message    Hello Dev from Vault!
```

More info: https://developer.hashicorp.com/vault/tutorials/auth-methods/approle

3. Start `ConfigServerVaultBackendApplication` in IntelliJ

expected exception: java.lang.IllegalStateException: No thread-bound request found: Are you referring to request attributes outside of an actual web request, or processing a request outside of the originally receiving thread? If you are actually operating within a web request and still receive this message, your code is probably running outside of DispatcherServlet: In this case, use RequestContextListener or RequestContextFilter to expose the current request.

See: https://stackoverflow.com/questions/24025924/java-lang-illegalstateexception-no-thread-bound-request-found-exception-in-asp

4. Verify that client configuration is exposed:

```bash
$ http :8888/config-server-client/dev
$ http :8888/config-server-client/prd 
```

Check in the logs:

```bash
2024-10-23T10:59:03.123+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : HTTP GET http://localhost:8200/v1/secret/data/config-server-client/dev
2024-10-23T10:59:03.123+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : Accept=[application/json, application/*+json]
2024-10-23T10:59:03.126+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : Response 200 OK
2024-10-23T10:59:03.126+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : Reading to [org.springframework.vault.support.VaultResponseSupport<com.fasterxml.jackson.databind.JsonNode>]
2024-10-23T10:59:03.137+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : HTTP GET http://localhost:8200/v1/secret/data/config-server-client
2024-10-23T10:59:03.137+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : Accept=[application/json, application/*+json]
2024-10-23T10:59:03.140+02:00 DEBUG 23155 --- [config-server-vault-backend-approle] [nio-8888-exec-1] o.s.web.client.RestTemplate              : Response 404 NOT_FOUND
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
2024-10-11T10:26:54.155+02:00  INFO 37630 --- [config-server-client] [nio-8080-exec-7] o.s.c.c.c.ConfigServerConfigDataLoader   : Located environment: name=config-server-client, profiles=[default], label=null, version=null, state=null
2024-10-11T10:26:54.165+02:00  INFO 37630 --- [config-server-client] [nio-8080-exec-7] o.s.c.c.c.ConfigServerConfigDataLoader   : Fetching config from server at : http://localhost:8888
2024-10-11T10:26:54.176+02:00  INFO 37630 --- [config-server-client] [nio-8080-exec-7] o.s.c.c.c.ConfigServerConfigDataLoader   : Located environment: name=config-server-client, profiles=[dev], label=null, version=null, state=null
2024-10-11T10:26:54.309+02:00  INFO 37630 --- [config-server-client] [nio-8080-exec-7] o.s.cloud.endpoint.RefreshEndpoint       : Refreshed keys : [message]
```

8. Verify that the client knows about the change:

```bash
$ http :8080/message
Hello Dev from Vault! - changed
```
