package ru.flux.flux.messenger.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(String message) {
        super(message);
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug(message);
    }
}
