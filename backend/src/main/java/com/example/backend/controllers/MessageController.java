package com.example.backend.controllers;

import com.example.backend.DTOs.SearchResultDTO;
import com.example.backend.models.Message;
import com.example.backend.services.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final SearchService searchService;

    public MessageController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<SearchResultDTO> searchMessages(
            @RequestParam(name="q", required = true) String query,
            @RequestParam(name = "type", required = true) String type
    ) {
        List<Message> result = switch (type) {
            case "text" -> searchService.searchText(query);
            case "vector" -> searchService.searchVector(query);
            default -> throw new IllegalArgumentException("Unknown search type.");
        };
        return result.stream().map(m -> new SearchResultDTO(m, m.getChat().getId(), m.getChat().getTitle())).toList();
    }
}
