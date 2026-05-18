package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasskeyOptionsRequest(
        @Pattern(regexp = "^\\+?[0-9]{10,15}$")
        @NotBlank String phone,
        String firstName,
        String lastName,
        String username
) {
}
