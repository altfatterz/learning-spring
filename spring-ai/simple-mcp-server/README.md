
# Simple MCP Server

- Start the application

## MCP Inspector 

- Interactive developer tool for testing and debugging MCP servers.

```bash
npx @modelcontextprotocol/inspector

Need to install the following packages:
@modelcontextprotocol/inspector@0.17.2
Ok to proceed? (y) y

npm warn deprecated node-domexception@1.0.0: Use your platform's native DOMException instead
Starting MCP inspector...
⚙️ Proxy server listening on localhost:6277
🔑 Session token: <TOKEN>
   Use this token to authenticate requests or set DANGEROUSLY_OMIT_AUTH=true to disable auth

🚀 MCP Inspector is up and running at:
   http://localhost:6274/?MCP_PROXY_AUTH_TOKEN=<TOKEN>

```

- Select `Transport Type`: Streamable HTTP
- URL: `localhost:8080/mcp`

### Test the MCP server with command line:

-w '%{http_code}' - print out the HTTP status code (200, 401, ...)
-w '%header{www-authenticate}' - print out the `WWW-Authenticate` header, if present

```bash
$ curl -XPOST  -w '%{http_code}\n%header{www-authenticate}' http://localhost:8080/mcp
# Will print out:
#
# 401
# Bearer resource_metadata=http://localhost:8080/.well-known/oauth-protected-resource/mcp
```

The `resource_metadata` is an extension parameter defined in the OAuth 2.0 “Protected Resource Metadata” spec. 
It points to a URL where the resource (your MCP endpoint) publishes metadata about itself.

```bash
$ curl http://localhost:8080/.well-known/oauth-protected-resource/mcp | jq .
{
  "resource": "http://localhost:8080/mcp",
  "authorization_servers": [
    "http://localhost:9000"
  ],
  "resource_name": "Spring MCP Resource Server",
  "bearer_methods_supported": [
    "header"
  ]
}

$ curl http://localhost:9000/.well-known/oauth-authorization-server | jq . 
{
  "issuer": "http://localhost:9000",
  "authorization_endpoint": "http://localhost:9000/oauth2/authorize",
  "device_authorization_endpoint": "http://localhost:9000/oauth2/device_authorization",
  "token_endpoint": "http://localhost:9000/oauth2/token",
  "token_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt",
    "tls_client_auth",
    "self_signed_tls_client_auth"
  ],
  "jwks_uri": "http://localhost:9000/oauth2/jwks",
  "response_types_supported": [
    "code"
  ],
  "grant_types_supported": [
    "authorization_code",
    "client_credentials",
    "refresh_token",
    "urn:ietf:params:oauth:grant-type:device_code",
    "urn:ietf:params:oauth:grant-type:token-exchange"
  ],
  "revocation_endpoint": "http://localhost:9000/oauth2/revoke",
  "revocation_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt",
    "tls_client_auth",
    "self_signed_tls_client_auth"
  ],
  "introspection_endpoint": "http://localhost:9000/oauth2/introspect",
  "introspection_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt",
    "tls_client_auth",
    "self_signed_tls_client_auth"
  ],
  "code_challenge_methods_supported": [
    "S256"
  ],
  "tls_client_certificate_bound_access_tokens": true,
  "dpop_signing_alg_values_supported": [
    "RS256",
    "RS384",
    "RS512",
    "PS256",
    "PS384",
    "PS512",
    "ES256",
    "ES384",
    "ES512"
  ],
  "registration_endpoint": "http://localhost:9000/oauth2/register"
}

```



## Build the jar and use it in an MCP client (like Claude Desktop) 

- Claude Desktop - https://www.claude.com/download

### Steps:
- Build the application first with `mvn clean package`
- Setup `claude_desktop_config.json`

```bash
$ cp claude_desktop_config_simple_mcp_server.json ~/Library/Application\ Support/Claude/claude_desktop_config.json
```

Here important thing is that we disable banner and console logging in order to allow STDIO transport to work. 
See how is done in the `claude_desktop_config.json`

- Start Claude Desktop (no error should occur)
- Ask question like:
  
```bash
$ get me Nolan Gouveia's latest videos

Claude wants to use investing-simplified-latest-videos from investing-simplified-latest videos
```

## Connect to Local MCP Server with Claude Desktop

Use `Filesystem Server`

```bash
$ npx @modelcontextprotocol/server-filesystem /Users/altfatterz/Desktop

npm warn Unknown user config "always-auth". This will stop working in the next major version of npm.
npm warn Unknown user config "email". This will stop working in the next major version of npm.
Need to install the following packages:
@modelcontextprotocol/server-filesystem@2025.8.21
Ok to proceed? (y) y

Secure MCP Filesystem Server running on stdio
```

### Let's configure it as an MCP Server

```bash
$ cp claude_desktop_config_filesystem_server.json ~/Library/Application\ Support/Claude/claude_desktop_config.json
$ cat ~/Library/Application\ Support/Claude/claude_desktop_config.json
```

View details: https://modelcontextprotocol.io/docs/develop/connect-local-servers

Note: Only grant access to directories you’re comfortable with Claude reading and modifying. 
The server runs with your user account permissions, so it can perform any file operations you can perform manually.

Claude Prompts:

```bash
$ What work-related files are in my downloads folder?
$ Can you write a poem and save it to my desktop?
$ Please organize all images on my desktop into a new folder called ‘Images’
```

Cleanup any running processes:

```bash
$ ps aux | grep server-filesystem
```

## Resources
- https://www.youtube.com/watch?v=MarSC2dFA9g
- https://www.youtube.com/watch?v=ik5-ukQPtyQ
- https://modelcontextprotocol.io/docs/getting-started/intro
- https://modelcontextprotocol.io/docs/tools/inspector
- https://spring.io/blog/2025/09/16/spring-ai-mcp-intro-blog