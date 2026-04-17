package ru.flux.flux.messenger.dto;

import java.util.UUID;

public record SharedGroupInfo(
        UUID id,
        String avatarUrl
) {
}
