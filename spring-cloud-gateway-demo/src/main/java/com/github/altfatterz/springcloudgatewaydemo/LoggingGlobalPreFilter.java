package com.github.altfatterz.springcloudgatewaydemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalPreFilter implements GlobalFilter {

    final Logger logger = LoggerFactory.getLogger(LoggingGlobalPreFilter.class);

    @Value("${fcs.db-contract-route:false}")
    private Boolean dbContractRoute;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        if (dbContractRoute) {
            logger.info("Connecting to DB with user: {}", cookies.get("FIN_TICKET"));
        } else {
            logger.info("Connecting to Finnova with user: {}", cookies.get("FIN_TICKET"));
        }
        logger.info("Global Pre Filter executed");
        return chain.filter(exchange);
    }
}