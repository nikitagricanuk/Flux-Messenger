package ru.flux.android;


import java.util.List;

public class ChatResponse {
    public String id;
    public String name;
    public String profilePicture;
    public String type; // "DIRECT" or "GROUP"
    public List<String> memberIds;
    public String lastMessage;
    public String lastMessageAt; // ISO-8601, e.g. "2026-04-12T14:19:23"
}
