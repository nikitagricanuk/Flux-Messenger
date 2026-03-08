package ru.flux.desktop.app;

public enum ScreenId {
    ALL_CHATS("all-chats");

    private final String id;

    ScreenId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
