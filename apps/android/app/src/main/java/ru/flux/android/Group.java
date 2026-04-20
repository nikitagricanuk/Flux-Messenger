package ru.flux.android;

public class Group {
    public String name;
    public String membersCount;
    public String date;
    public String avatarUrl;

    public Group(String name, String membersCount, String date, String avatarUrl) {
        this.name = name;
        this.membersCount = membersCount;
        this.date = date;
        this.avatarUrl = avatarUrl;
    }
}