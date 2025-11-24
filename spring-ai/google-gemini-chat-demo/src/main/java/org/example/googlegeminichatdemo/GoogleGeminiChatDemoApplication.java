package org.example.googlegeminichatdemo;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GoogleGeminiChatDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoogleGeminiChatDemoApplication.class, args);
	}

	@Bean
	public SimpleLoggerAdvisor simpleLoggerAdvisor() {
		return new SimpleLoggerAdvisor();
	}
}
