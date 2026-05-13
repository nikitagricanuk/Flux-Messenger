package ru.flux.android.core.data;

public class DisplayItem {
    public final String name;
    public final String subtitle;
    public final String avatarUrl;
    public final Runnable onClick;

    public DisplayItem(String name, String subtitle, String avatarUrl, Runnable onClick) {
        this.name = name;
        this.subtitle = subtitle;
        this.avatarUrl = avatarUrl;
        this.onClick = onClick;
    }
}