package ru.flux.flux.messenger.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(UUID id) {
        super(
                String.format("Chat with id %s not found", id)
        );
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug("Chat with id {} not found", id);
    }
}
