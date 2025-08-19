package com.example.backend.Events;

import com.example.backend.models.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

// a custom event to publish when new messages are created
@Getter
public class MessagesCreatedEvent extends ApplicationEvent {
    private final List<Message> messages;

    public MessagesCreatedEvent(Object source, List<Message> messages) {
        super(source);
        this.messages = messages;
    }
}
