package com.example.backend.init;

import com.example.backend.models.LLModel;
import com.example.backend.repositories.LLModelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// The primary purpose of CommandLineRunner is to run code once the application is ready
// Here I'm going to create 2 LLMs in the DB which are needed for the app
@Component
public class LLMDataInitializer implements CommandLineRunner {

    private final LLModelRepository llModelRepository;

    public LLMDataInitializer(LLModelRepository llModelRepository) {
        this.llModelRepository = llModelRepository;
    }

    @Override
    public void run(String... args) {
        LLModel gpt = new LLModel(1, "gpt-4o-mini", "OpenAI");
        LLModel gemini = new LLModel(2, "Gemini", "Google");
        llModelRepository.saveAll(List.of(gpt, gemini));
    }
}
