package org.example.springaiollamademo;

import org.example.springaiollamademo.functions.WeatherConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WeatherConfigProperties.class)
public class SpringAiOllamaDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiOllamaDemoApplication.class, args);
	}

}
