package ru.flux.android.data;

import java.util.List;
import java.util.UUID;

public class CreateChatRequest {
    public String type;
    public String name;
    public List<UUID> memberIds;
    public String avatarUrl;

    public CreateChatRequest(String type, String name, List<UUID> memberIds) {
        this.type = type;
        this.name = name;
        this.memberIds = memberIds;
    }
}