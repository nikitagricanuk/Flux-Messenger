package ru.flux.flux.messenger.dto;

import ru.flux.flux.messenger.ChatType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String name,
        String profilePicture,
        ChatType type,
        List<UUID> memberIds,
        String lastMessage,
        LocalDateTime lastMessageAt
) {}
