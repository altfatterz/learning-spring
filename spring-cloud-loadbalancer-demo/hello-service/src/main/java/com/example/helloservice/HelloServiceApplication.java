package com.example.helloservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@SpringBootApplication
public class HelloServiceApplication {

	private static Logger logger = LoggerFactory.getLogger(HelloServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(HelloServiceApplication.class, args);
	}

	@GetMapping("/greeting")
	public String greet() {
		logger.info("Access /greeting");
		List<String> greetings = Arrays.asList("Hi there", "Greetings", "Salutations");
		Random rand = new Random();
		int randomNum = rand.nextInt(greetings.size());
		return greetings.get(randomNum);
	}

}
