package ru.flux.flux.messenger.dto;

import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.MessageStatus;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID chatId,
        UUID senderId,
        String senderName,
        String senderAvatar,
        String text,
        MessageStatus status,
        Instant createdAt,
        String mediaUrl,
        String mediaType 
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getFirstName() + " " +
                        (message.getSender().getLastName() != null
                                ? message.getSender().getLastName() : ""),
                message.getSender().getAvatarUrl(),
                message.getText(),
                message.getStatus(),
                message.getCreatedAt(),
                message.getMediaUrl(),   
                message.getMediaType() 
        );
    }
}