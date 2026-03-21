package ru.flux.flux.messenger.exceptions;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(UUID id) {
        super(
                String.format("Profile with id %s not found", id)
        );
    }
}
