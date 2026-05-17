package ru.flux.flux.messenger.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID chatId,
        String text,      
        String mediaUrl, 
        String mediaType 
) {}