## Secure MCP

### Create a New OAuth App in GitHub

- I have created a `mcp-authorization-server` https://github.com/settings/developers 
- Set the `Authorization callback URL` to `http://localhost:9000/login/oauth2/code/github`
- Set the following environment variables on the `mcp-authorization-server` 

```bash
$ GITHUB_CLIENT_ID=<TODO>;GITHUB_CLIENT_SECRET=<TODO>
```

Before testing:
- Revoke the token from the created `mcp-authorization-server` Authorized OAuth App at https://github.com/settings/applications
- Logout your GitHub account

### Start the applications

- First start `mcp-authorization-server` - federates authentication to GitHub 
- Then start `secure-mcp-server-with-oauth2`

### Start the MCP Inspector

```bash
npx @modelcontextprotocol/inspector
```

Transport Type: `Streamable Http`
URL: http://localhost:8080/mcp

- Hit `Connect` 
- You will be redirected to Login to GitHub
- Then you need to approve authorization
- After connecting hit `Tools` and run the available tools


### Tools

Open the Developer Tools in the MCP Inspector:

#### Listing the Tools:

```bash

# url parameters url=urlhttp://localhost:8080/mcp&transportType=streamable-http
$ POST http://localhost:6277/mcp?url=http%3A%2F%2Flocalhost%3A8080%2Fmcp&transportType=streamable-http
--header "Authorization: Bearer <TOKEN>"

$ jwt decode $TOKEN

Token header
------------
{
  "alg": "RS256",
  "kid": "98358bac-72f0-431a-88e5-48fbf29f7fd0"
}

Token claims
------------
{
  "aud": "http://localhost:8080/mcp",
  "exp": 1764666513,
  "iat": 1764666213,
  "iss": "http://localhost:9000",
  "jti": "335619e3-6678-4562-b546-3d4b2c5cba17",
  "nbf": 1764666213,
  "sub": "altfatterz"
}
```

#### Calling the getCurrentUser Tool

```bash
# url parameters url=urlhttp://localhost:8080/mcp&transportType=streamable-http
$ POST http://localhost:6277/mcp?url=http%3A%2F%2Flocalhost%3A8080%2Fmcp&transportType=streamable-http
--header "Authorization: Bearer <TOKEN>"
```

with payload:

```json
{
  "method": "tools/call",
  "params": {
    "name": "getCurrentUser",
    "arguments": {},
    "_meta": {
      "progressToken": 3
    }
  },
  "jsonrpc": "2.0",
  "id": 3
}
```


Check what is running on a port:
```bash
$ lsof -i :6277
```

Resources:
- https://spring.io/blog/2025/09/30/spring-ai-mcp-server-security
- How to Secure your MCP Servers with Spring Security 🔐 & Spring AI 🤖 https://www.youtube.com/watch?v=Xiw4bMD3SOg
- Securing MCP Servers by Daniel Garnier Moiroux (Devoxx 2025): https://www.youtube.com/watch?v=O6G6ufT03fk