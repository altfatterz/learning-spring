package org.example.openaichatdemo;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OpenaiChatDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenaiChatDemoApplication.class, args);
	}

	@Bean
	public SimpleLoggerAdvisor simpleLoggerAdvisor() {
		return new SimpleLoggerAdvisor();
	}
}
