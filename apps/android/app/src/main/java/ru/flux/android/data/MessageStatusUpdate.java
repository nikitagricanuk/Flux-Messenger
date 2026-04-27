package ru.flux.android.data;

import java.util.UUID;

public class MessageStatusUpdate {
    private final UUID chatId;
    private final UUID messageId;
    private final String status;

    public MessageStatusUpdate(UUID chatId, UUID messageId, String status) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.status = status;
    }

    public UUID getChatId() { return chatId; }
    public UUID getMessageId() { return messageId; }
    public String getStatus() { return status; }
}