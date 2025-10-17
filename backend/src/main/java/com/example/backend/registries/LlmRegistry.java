package com.example.backend.registries;

import com.example.backend.services.LlmService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmRegistry {
    private final Map<String, LlmService> mapByProvider;

    public LlmRegistry(List<LlmService> llmServices) {
        this.mapByProvider = llmServices.stream()
                .collect(Collectors.toUnmodifiableMap(LlmService::provider, Function.identity()));
    }

    public LlmService getLlmService(String provider) {
        LlmService service = mapByProvider.get(provider);
        if (service == null) {
            throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        }
        return service;
    }
}
