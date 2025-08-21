package com.example.backend.repositories;


import com.example.backend.models.Chat;
import com.example.backend.models.Message;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatOrderByIdDesc(Chat chat, Limit limit);
}
