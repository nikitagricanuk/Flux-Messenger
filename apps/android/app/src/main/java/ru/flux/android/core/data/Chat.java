package ru.flux.android.core.data;

public class Chat {
    public String id;
    public String name;
    public String lastMessage;
    public String avatarUrl;
    public String time;
    public String type; // "dm" or "group"

    public Chat(String id, String name, String lastMessage, String avatarUrl, String time, String type) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.avatarUrl = avatarUrl;
        this.time = time;
        this.type = type;
    }
}
