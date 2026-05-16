package ru.flux.flux.messenger.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug(message);
    }
}
