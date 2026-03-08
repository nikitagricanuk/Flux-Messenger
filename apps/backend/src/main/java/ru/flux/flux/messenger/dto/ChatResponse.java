package ru.flux.flux.messenger.dto;

import ru.flux.flux.messenger.ChatType;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String name,
        ChatType type,
        List<UUID> memberIds
) {}
