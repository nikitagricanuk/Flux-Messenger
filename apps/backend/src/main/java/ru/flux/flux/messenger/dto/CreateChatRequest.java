package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.flux.flux.messenger.ChatType;
import ru.flux.flux.messenger.validation.ValidChatRequest;

import java.util.List;
import java.util.UUID;

@ValidChatRequest
public record CreateChatRequest(
        String name,
        String avatarUrl,
        @NotNull ChatType type,
        @NotEmpty List<UUID> memberIds
) {}
