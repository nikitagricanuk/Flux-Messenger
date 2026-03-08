package ru.flux.desktop.chats.api;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID id,
        String name,
        String type,
        List<UUID> memberIds
) {}
