package com.example.backend.controllers;

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

    @GetMapping
    public List<Message> searchMessages(@RequestParam(name="q", required = true) String query) {
        return searchService.search(query);
    }
}
