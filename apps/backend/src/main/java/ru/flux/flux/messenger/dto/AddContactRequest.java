package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddContactRequest(
        String firstName,
        String lastName,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone
) {
}
