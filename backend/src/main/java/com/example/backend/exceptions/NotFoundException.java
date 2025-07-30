package com.example.backend.exceptions;

// Although there is the ResponseStatusException that could have been used instead
// Since I throw this from the service layer, to maintain the separation of concerns principle
// I created this custom exception
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}
