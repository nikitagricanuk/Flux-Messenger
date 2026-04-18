package ru.flux.flux.messenger.exceptions;

public class PasskeyVerificationException extends RuntimeException {
    public PasskeyVerificationException(String message) {
        super(message);
    }

    public PasskeyVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
