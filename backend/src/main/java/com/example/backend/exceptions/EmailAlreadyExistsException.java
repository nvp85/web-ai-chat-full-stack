package com.example.backend.exceptions;

public class EmailAlreadyExistsException extends Exception{
    public EmailAlreadyExistsException() {
        super("User with this email already exists");
    }
}
