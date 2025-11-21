## Simple MCP client


Currently it doesn't work.

```bash
$ http :8080/api/chat\?prompt="What are Dan Vega's latest Youtube videos"

HTTP/1.1 200
Connection: keep-alive
Content-Length: 880
Content-Type: text/plain;charset=UTF-8
Date: Fri, 21 Nov 2025 08:59:49 GMT
Keep-Alive: timeout=60

Here are Dan Vega's latest YouTube videos:

1. **[Spring Boot 4's Built-in Resilience Features: Say Goodbye to External Dependencies!](https://www.youtube.com/watch?v=CT1wGTwOfg0)**
   Published on: November 13, 2025

2. **[Jackson 3 Support is HERE: What's New in Spring Framework 7 & Spring Boot 4](https://www.youtube.com/watch?v=4cvP_qroLH4)**
   Published on: November 10, 2025

3. **[Build AI-Powered Apps with MCP Clients in Spring AI](https://www.youtube.com/watch?v=TSFkdlreRMQ)**
   Published on: November 7, 2025

4. **[Creating REST Clients in Spring Boot 4 Just Got EASIER!](https://www.youtube.com/watch?v=TEd5e4Thu7M)**
   Published on: November 6, 2025

5. **[⛔ Stop NullPointerExceptions Before Production in Spring Boot 4 with Null Safety](https://www.youtube.com/watch?v=QlGnaRoujL8)**
   Published on: November 5, 2025

Feel free to check them out!
```

I see in the logs there is an exception:

```bash
io.modelcontextprotocol.spec.McpTransportException: Invalid SSE response. Status code: 503 Line: <!DOCTYPE html>
	at io.modelcontextprotocol.client.transport.ResponseSubscribers$SseLineSubscriber.hookOnNext(ResponseSubscribers.java:185) ~[mcp-core-0.16.0.jar:0.16.0]
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Assembly trace from producer [reactor.core.publisher.FluxCreate] :
	reactor.core.publisher.Flux.create(Flux.java:650)
```

## Resources:

- https://mcp.danvega.dev/
- https://www.youtube.com/watch?v=TSFkdlreRMQ
- https://www.danvega.dev/blog/spring-ai-mcp-client
