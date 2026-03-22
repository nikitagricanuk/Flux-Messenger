package ru.flux.flux.messenger.exceptions;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContactNotFoundException extends RuntimeException{
    public ContactNotFoundException(UUID id) {
        super(
                String.format("Contact with id %s not found", id)
        );
    }
}
