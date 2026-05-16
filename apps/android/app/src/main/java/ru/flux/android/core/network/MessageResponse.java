package ru.flux.android.core.network;

import java.util.UUID;

public class MessageResponse {
    public UUID id;
    public UUID chatId;
    public UUID senderId;
    public String senderName;
    public String senderAvatar;
    public String text;
    public String status;
    public String createdAt;
}