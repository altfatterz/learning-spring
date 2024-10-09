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
path "secret/data/config-server-client/dev" {
  capabilities = [ "read" ]
}
path "secret/data/config-server-client/*" {
  capabilities = [ "read" ]
}
EOF
Success! Uploaded policy: config-server-client-policy
$ vault policy list
$ vault policy read config-server-client-policy
```

#### Create a `config-server-client-role` role 

```bash
$ vault write auth/approle/role/config-server-client-role token_policies="config-server-client-policy" \
    token_ttl=1h token_max_ttl=4h
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
token_ttl                  1h
token_type                 default
```

#### Get RoleId and SecretId

```bash
$ vault read auth/approle/role/config-server-client-role/role-id
Key        Value
---        -----
role_id    2b12b3e8-65f7-fe94-2438-51793ec109c9

# generate a new SecretID
$ vault write -force auth/approle/role/config-server-client-role/secret-id
Key                   Value
---                   -----
secret_id             f6190cb1-ca36-a186-d339-010caddba9b2
secret_id_accessor    569fc003-afc9-9bbd-b324-3b9d2f70f0af
secret_id_num_uses    0
secret_id_ttl         0s
```

#### Login with RoleId and SecretId

Get a new shell

```bash
$ docker exec -it vault sh
$ export VAULT_ADDR="http://127.0.0.1:8200"
$ vault kv get secret/data/config-server-client/dev
URL: GET http://127.0.0.1:8200/v1/sys/internal/ui/mounts/secret/data/config-server-client/dev
Code: 403. Errors: permission denied
// login 
$ vault write auth/approle/login role_id="2b12b3e8-65f7-fe94-2438-51793ec109c9" \
    secret_id="f6190cb1-ca36-a186-d339-010caddba9b2"
Key                     Value
---                     -----
token                   hvs.CAESIG8ZIVzza-2O6XLgxX25Ywq5bfE7-Zzw8C0m8iY_l85XGh4KHGh2cy5vMzhPUjNPYW9IaGl2UlBpa2hUR1RqV3o
token_accessor          mEPFcVGEu40UoVGw3Gi0OJ1E
token_duration          1h
token_renewable         true
token_policies          ["config-server-client-policy" "default"]
identity_policies       []
policies                ["config-server-client-policy" "default"]
token_meta_role_name    config-server-client-role

// store the token
export APP_TOKEN="hvs.CAESIG8ZIVzza-2O6XLgxX25Ywq5bfE7-Zzw8C0m8iY_l85XGh4KHGh2cy5vMzhPUjNPYW9IaGl2UlBpa2hUR1RqV3o"
    
// read secrets using the AppRole token
VAULT_TOKEN=$APP_TOKEN vault kv get secret/data/config-server-client/dev
URL: GET http://127.0.0.1:8200/v1/secret/data/data/config-server-client/dev
Code: 403. Errors: permission denied -- WHY?????
```

More info:  

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
