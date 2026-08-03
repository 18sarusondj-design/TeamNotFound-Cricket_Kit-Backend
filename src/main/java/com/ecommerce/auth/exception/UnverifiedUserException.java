package com.ecommerce.auth.exception;

public class UnverifiedUserException extends RuntimeException {
    private final String email;

    public UnverifiedUserException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
