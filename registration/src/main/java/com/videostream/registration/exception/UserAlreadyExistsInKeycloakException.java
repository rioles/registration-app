package com.videostream.registration.exception;


public class UserAlreadyExistsInKeycloakException extends RuntimeException {
    public UserAlreadyExistsInKeycloakException(String message) {
        super(message);
    }
}
