package ru.flux.android.core.data;

public class Message {
    public String id;
    public String text;
    public String senderId;
    public String senderName;
    public String senderAvatar;
    public String time;
    public boolean isOutgoing;
    public String mediaUrl;
    public String mediaType;

    public Message(String id, String text, String senderId, String senderName,
                   String senderAvatar, String time, boolean isOutgoing,
                   String mediaUrl, String mediaType) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.time = time;
        this.isOutgoing = isOutgoing;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }
}
