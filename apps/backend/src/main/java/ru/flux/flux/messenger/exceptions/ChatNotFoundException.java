package ru.flux.flux.messenger.exceptions;

import java.util.UUID;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(UUID id) {
        super(
                String.format("Chat with id %s not found", id)
        );
    }
}
