package com.example.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // Lombok will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok will generate a no-args constructor
public class Chat {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who owns the chat

    private String title;
    private long lastModified; // Timestamp in milliseconds

    @ManyToOne
    @JoinColumn(name = "llm_id", nullable = false)
    private LLModel llModel; // The LLM used for the chat

    public Chat(User user, String title, long lastModified, LLModel llModel) {
        this.user = user;
        this.title = title;
        this.lastModified = lastModified;
        this.llModel = llModel;
    }
}
