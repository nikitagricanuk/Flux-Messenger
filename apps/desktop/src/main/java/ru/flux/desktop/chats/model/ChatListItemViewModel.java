package ru.flux.desktop.chats.model;

import java.util.Objects;
import java.util.UUID;

public record ChatListItemViewModel(
        UUID id,
        String title,
        String subtitle,
        String meta,
        String badgeText,
        int accentSeed
) {
    public ChatListItemViewModel {
        Objects.requireNonNull(id);
        Objects.requireNonNull(title);
        Objects.requireNonNull(subtitle);
        Objects.requireNonNull(meta);
        Objects.requireNonNull(badgeText);
    }
}
