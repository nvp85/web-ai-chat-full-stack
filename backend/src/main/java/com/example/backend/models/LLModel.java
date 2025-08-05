package com.example.backend.models;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Data // Lombok will generate getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok will generate a no-args constructor
@AllArgsConstructor
public class LLModel {
    @Id
    private int id;

    private String name; // Name of the LLM
    private String provider; // Provider of the LLM (e.g., OpenAI, Google)

    public LLModel(String name, String provider) {
        this.name = name;
        this.provider = provider;
    }
}
