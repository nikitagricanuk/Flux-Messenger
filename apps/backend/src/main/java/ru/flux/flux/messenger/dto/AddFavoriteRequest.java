package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddFavoriteRequest(
        @NotBlank UUID id
) {
}
