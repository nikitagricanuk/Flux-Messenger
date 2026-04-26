package ru.flux.android.data;

import java.util.UUID;

public class SendMessageRequest {
    public UUID chatId;
    public String text;

    public SendMessageRequest(UUID chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }
}