package com.example.backend;

import com.google.genai.Client;
import com.openai.client.OpenAIClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.profiles.active=test")
class AiChatBackendApplicationTests {

	@MockitoBean
	OpenAIClient openAIClient;

	@MockitoBean
	Client googleAIClient;

	@Test
	void contextLoads() {

	}
}
