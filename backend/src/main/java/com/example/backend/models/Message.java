package com.example.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.search.engine.backend.types.VectorSimilarity;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtract;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

@Entity
@Indexed
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

    @FullTextField(analyzer = "english")
    @Column(columnDefinition = "TEXT")
    private String content; // The content of the message
    private String role; // The role of the message (e.g., "user", "assistant")

    @Transient
    @JsonIgnore
    @IndexingDependency(derivedFrom = @ObjectPath(@PropertyValue(propertyName = "content")), extraction = @ContainerExtraction(extract = ContainerExtract.NO))
    @VectorField(dimension = 768, vectorSimilarity = VectorSimilarity.COSINE)
    private float[] embedding;

    @JsonIgnore
    @GenericField
    @Transient
    @IndexingDependency(derivedFrom = @ObjectPath({
            @PropertyValue(propertyName = "chat"),
            @PropertyValue(propertyName = "user"),
            @PropertyValue(propertyName = "id")}))
    private int ownerId;

    public Message(String content, String role, Chat chat) {
        this.content = content;
        this.role = role;
        this.chat = chat;
    }

    public Message(String content, String role) {
        this.content = content;
        this.role = role;
    }

    public int getOwnerId() {
        return chat.getUser().getId();
    }
}
