package ru.flux.flux.messenger.dto;

import ru.flux.flux.messenger.MessageStatus;
import java.util.UUID;

public record MessageStatusUpdate(
        UUID chatId,
        UUID messageId,
        MessageStatus status
) {}