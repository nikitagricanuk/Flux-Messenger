package ru.flux.flux.messenger.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super(
                String.format("User with id %s not found", id)
        );
    }
    public UserNotFoundException(String phone) {
        super(
                String.format("User with phone %s not found", phone)
        );
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.debug("User with phone {} not found", phone);
    }
}
