package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data // Lombok will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok will generate a no-args constructor
public class Chat {
    @Id
    private UUID id; // Unique identifier for the chat (comes from the frontend)

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who owns the chat

    private String title;
    private long lastModified; // Timestamp in milliseconds

    @ManyToOne
    @JoinColumn(name = "llm_id", nullable = false)
    private LLModel llModel; // The LLM used for the chat

    @JsonIgnore
    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE, orphanRemoval = true) // remove the messages if the chat is deleted
    @OrderBy("id ASC")
    private List<Message> messages; // List of messages in the chat

    public Chat(UUID id, User user, String title, long lastModified, LLModel llModel) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.lastModified = lastModified;
        this.llModel = llModel;
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setChat(this); // Set the chat for the message
    }
}
