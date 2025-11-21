package org.example.simplemcpclient;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SimpleMcpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleMcpClientApplication.class, args);
	}

	@Bean
	public SimpleLoggerAdvisor simpleLoggerAdvisor() {
		return new SimpleLoggerAdvisor();
	}
}
