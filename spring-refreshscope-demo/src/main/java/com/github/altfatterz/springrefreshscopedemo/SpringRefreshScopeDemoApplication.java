package com.github.altfatterz.springrefreshscopedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
public class SpringRefreshScopeDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRefreshScopeDemoApplication.class, args);
	}

}
