package com.github.altfatterz.hellocli.config;

import com.github.altfatterz.hellocli.client.DadJokeClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class DadJokeClientConfig {

    @Bean
    public DadJokeClient dadJokeClient(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl("https://icanhazdadjoke.com").build();

        // Create the proxy factory
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(DadJokeClient.class);
    }
}