package com.example.backend.DTOs;


import com.example.backend.models.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDTO {
    private Message message;
    private UUID chatId;
    private String chatTitle;
}
