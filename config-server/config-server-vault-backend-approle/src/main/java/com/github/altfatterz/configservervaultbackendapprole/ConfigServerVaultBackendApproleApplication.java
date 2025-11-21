package com.github.altfatterz.configservervaultbackendapprole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerVaultBackendApproleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerVaultBackendApproleApplication.class, args);
    }

}
