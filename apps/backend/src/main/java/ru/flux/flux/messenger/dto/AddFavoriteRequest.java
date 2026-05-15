package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddFavoriteRequest(
        @NotNull UUID id
) {
}
