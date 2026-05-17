package ru.flux.android.core.network;

import java.util.UUID;

public class SendMessageRequest {
    public UUID chatId;
    public String text;
    public String mediaUrl;
    public String mediaType;

    public SendMessageRequest(UUID chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    public SendMessageRequest(UUID chatId, String text, String mediaUrl, String mediaType) {
        this.chatId = chatId;
        this.text = text;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }
}