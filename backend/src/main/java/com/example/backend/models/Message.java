package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // Lombok will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok will generate a no-args constructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat; // The chat to which this message belongs

    @Column(columnDefinition = "TEXT")
    private String content; // The content of the message
    private String role; // The role of the message (e.g., "user", "assistant")

    public Message(String content, String role, Chat chat) {
        this.content = content;
        this.role = role;
        this.chat = chat;
    }

    public Message(String content, String role) {
        this.content = content;
        this.role = role;
    }
}
