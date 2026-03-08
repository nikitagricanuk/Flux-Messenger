package ru.flux.desktop.chats.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import ru.flux.desktop.chats.api.ChatResponse;

class ChatListItemMapperTest {
    @Test
    void mapsDirectChatsWithoutNameToFallbackTitle() {
        ChatResponse response = new ChatResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                null,
                "DIRECT",
                List.of(UUID.randomUUID(), UUID.randomUUID())
        );

        ChatListItemViewModel viewModel = ChatListItemMapper.map(List.of(response)).getFirst();

        assertEquals("Direct chat (2)", viewModel.title());
        assertEquals("Direct conversation", viewModel.subtitle());
        assertEquals("2 people", viewModel.meta());
        assertEquals("DC", viewModel.badgeText());
    }
}
