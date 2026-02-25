package com.github.altfatterz.configservervaultagentinjector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerVaultAgentInjectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerVaultAgentInjectorApplication.class, args);
	}

}
