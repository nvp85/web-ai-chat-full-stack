package com.example.backend;

import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.openai.client.OpenAIClient;


@SpringBootApplication
public class AiChatBackendApplication {

	@Bean
	public OpenAIClient openAIClient() {
		return OpenAIOkHttpClient.fromEnv();
	}

	public static void main(String[] args) {
		SpringApplication.run(AiChatBackendApplication.class, args);
	}

}
