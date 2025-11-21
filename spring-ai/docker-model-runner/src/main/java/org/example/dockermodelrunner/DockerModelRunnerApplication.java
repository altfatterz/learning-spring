package org.example.dockermodelrunner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DockerModelRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerModelRunnerApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
        return args -> {
            ChatClient client = builder.build();
            String response = client.prompt("What can you do with Docker model runner?").call().content();
            System.out.println(response);
        };
    }

}
