package com.example.backend.controllers;

import com.example.backend.DTOs.SearchResultDTO;
import com.example.backend.exceptions.NotFoundException;
import com.example.backend.models.Message;
import com.example.backend.services.SearchService;
import com.example.backend.services.UserService;
import eu.fraho.spring.securityJwt.base.dto.JwtUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final SearchService searchService;
    private final UserService userService;

    public MessageController(SearchService searchService, UserService userService) {
        this.userService = userService;
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<SearchResultDTO> searchMessages(
            @RequestParam(name="q", required = true) String query,
            @RequestParam(name = "type", required = true) String type,
            @AuthenticationPrincipal JwtUser jwtUser
    ) throws NotFoundException {
        int ownerId = userService.getUserByEmail(jwtUser.getUsername()).getId();
        List<Message> result = switch (type) {
            case "text" -> searchService.searchText(query, ownerId);
            case "vector" -> searchService.searchVector(query, ownerId);
            default -> throw new IllegalArgumentException("Unknown search type.");
        };
        return result.stream().map(m -> new SearchResultDTO(m, m.getChat().getId(), m.getChat().getTitle())).toList();
    }
}
