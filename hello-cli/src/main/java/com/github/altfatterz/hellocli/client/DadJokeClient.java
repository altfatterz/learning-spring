package com.github.altfatterz.hellocli.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json")
public interface DadJokeClient {

    @GetExchange("/")
    DadJokeResponse getRandomJoke();
}