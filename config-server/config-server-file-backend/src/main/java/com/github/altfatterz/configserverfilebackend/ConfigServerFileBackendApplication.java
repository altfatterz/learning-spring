package com.github.altfatterz.configserverfilebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerFileBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerFileBackendApplication.class, args);
	}

}
