## Secure MCP

### Start the applications

- First start `mcp-authorization-server` - federates authentication to GitHub 
- Then start `secure-mcp-server-with-oauth2`

### Test it with MCP Inspector

Before testing:
- Logout your GitHub account
- Revoke all user tokens from the created `mcp-authorization-server` Github OAuth App

```bash
npx @modelcontextprotocol/inspector
```

Transport Type: `Streamable Http`
URL: http://localhost:8080/mcp

- Hit `Connect` 
- You will be redirected to Login to GitHub
- Then you need to approve authorization
- After connecting hit `Tools` and run the available tools


Check what is running on a port:
```bash
$ lsof -i :6277
```

Resources:
- https://spring.io/blog/2025/09/30/spring-ai-mcp-server-security
- How to Secure your MCP Servers with Spring Security 🔐 & Spring AI 🤖 https://www.youtube.com/watch?v=Xiw4bMD3SOg
- Securing MCP Servers by Daniel Garnier Moiroux (Devoxx 2025): https://www.youtube.com/watch?v=O6G6ufT03fk