package ru.flux.android;

public class Message {
    public String id;
    public String text;
    public String senderId;
    public String senderName;
    public String senderAvatar;
    public String time;
    public boolean isOutgoing;

    public Message(String id, String text, String senderId,
                   String senderName, String senderAvatar,
                   String time, boolean isOutgoing) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.time = time;
        this.isOutgoing = isOutgoing;
    }
}