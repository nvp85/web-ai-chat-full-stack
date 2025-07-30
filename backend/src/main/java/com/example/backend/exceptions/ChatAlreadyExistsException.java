package com.example.backend.exceptions;

public class ChatAlreadyExistsException extends Exception {
    public ChatAlreadyExistsException() {
        super("Chat with this ID already exists.");
    }
}
