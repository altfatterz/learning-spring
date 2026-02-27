package com.github.altfatterz.configservervaultagentinit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerVaultAgentInitApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerVaultAgentInitApplication.class, args);
    }

}
