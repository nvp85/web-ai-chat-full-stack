package com.example.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.models.Chat;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
}
