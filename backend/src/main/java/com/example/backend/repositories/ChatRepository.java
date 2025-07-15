package com.example.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.models.Chat;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    public List<Chat> findAllByUserEmail(String email); // Method to find all chats by user email
}
