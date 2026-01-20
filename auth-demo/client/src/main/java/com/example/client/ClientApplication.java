package com.example.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ClientApplication {

    private static Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb) {
        return rlb
                .routes()
                .route(rs -> rs
                        .path("/")
                        // we act as an oauth client and get a token, and we send that token back to the resource server - here the greeting-service
                        .filters(f ->
                                // tokenRelay() looks up the JWT in the ReactiveOAuth2AuthorizedClientService and adds the Authorization header
                                // in our case InMemoryReactiveOAuth2AuthorizedClientService
                                f.tokenRelay()
                                        // It reads the Authorization header from the ServerWebExchange and prints it.
                                        .filter((exchange, chain) -> {
                                            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                                            logger.info("Relayed Token: {}", authHeader);
                                            return chain.filter(exchange);
                                        }))
                        // Netty sends the modified request to http://localhost:8081
                        .uri("http://localhost:8081")
                ).build();
    }
}
