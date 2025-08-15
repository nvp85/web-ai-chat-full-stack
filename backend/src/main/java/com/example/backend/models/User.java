package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.util.List;

@Entity
@Data // Lombok will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok will generate a no-args constructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    @Column(nullable = false)
    private String password; // Password is stored securely (hashed)
    @KeywordField
    @Column(unique=true, nullable = false)
    private String email;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lastModified DESC")
    private List<Chat> chats; // if user gets deleted, so do their chats

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
