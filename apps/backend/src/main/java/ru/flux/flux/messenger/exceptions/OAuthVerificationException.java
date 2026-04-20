package ru.flux.flux.messenger.exceptions;

public class OAuthVerificationException extends RuntimeException {
    public OAuthVerificationException(String message) {
        super(message);
    }

    public OAuthVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
