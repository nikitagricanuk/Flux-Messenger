package ru.flux.desktop.chats.model;

import java.util.List;
import ru.flux.desktop.chats.api.ChatResponse;

public final class ChatListItemMapper {
    private ChatListItemMapper() {
    }

    public static List<ChatListItemViewModel> map(List<ChatResponse> chats) {
        return chats.stream().map(ChatListItemMapper::mapSingle).toList();
    }

    static ChatListItemViewModel mapSingle(ChatResponse chat) {
        String title = deriveTitle(chat);
        String type = chat.type() == null ? "chat" : chat.type().toLowerCase();
        int members = chat.memberIds() == null ? 0 : chat.memberIds().size();
        String subtitle = switch (type) {
            case "group" -> members + " members";
            case "direct" -> members > 0 ? "Direct conversation" : "Private chat";
            default -> "Conversation";
        };
        String meta = members > 0 ? members + " people" : type;
        return new ChatListItemViewModel(
                chat.id(),
                title,
                subtitle,
                meta,
                initials(title),
                Math.abs(chat.id().hashCode())
        );
    }

    private static String deriveTitle(ChatResponse chat) {
        if (chat.name() != null && !chat.name().isBlank()) {
            return chat.name().trim();
        }
        int memberCount = chat.memberIds() == null ? 0 : chat.memberIds().size();
        return memberCount > 0 ? "Direct chat (" + memberCount + ")" : "Unnamed chat";
    }

    private static String initials(String value) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }
}
