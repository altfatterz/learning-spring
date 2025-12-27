package com.github.altfatterz.hellocli.commands;

import com.github.altfatterz.hellocli.client.DadJokeClient;
import com.github.altfatterz.hellocli.client.DadJokeResponse;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class DadJokeCommands {

    private final DadJokeClient dadJokeClient;

    public DadJokeCommands(DadJokeClient dadJokeClient) {
        this.dadJokeClient = dadJokeClient;
    }

    @ShellMethod(key = "joke", value = "I will tell you a joke")
    public String getRandomJoke() {
        DadJokeResponse response = dadJokeClient.getRandomJoke();
        return response.joke();
    }

}
