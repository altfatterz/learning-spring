package com.github.altfatterz.hellocli.client;

public record DadJokeResponse(
        String id,
        String joke,
        Integer status
) {}