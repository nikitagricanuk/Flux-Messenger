package ru.flux.flux.messenger.exceptions;

public class RegistrationTokenExpiredException extends RuntimeException {
    public RegistrationTokenExpiredException(String message) {
        super(message);
    }
}
