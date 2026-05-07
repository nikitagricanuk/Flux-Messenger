package ru.flux.android.core.network;

public class CreateChatRequest {
    public String name;
    public String avatarUrl;
    public String[] memberIds;
    public String type;

    public CreateChatRequest(String type, String[] memberIds) {
        this.type = type;
        this.memberIds = memberIds;
    }
}
