package ru.flux.android.core.data;

import java.util.List;

public class Group {
    public String id;
    public String name;
    public String avatarUrl;
    public List<String> memberIds;

    public Group(String id, String name, String avatarUrl, List<String> memberIds) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.memberIds = memberIds;
    }
}