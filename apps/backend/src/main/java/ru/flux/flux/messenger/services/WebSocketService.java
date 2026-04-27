package ru.flux.flux.messenger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.dto.MessageResponse;
import ru.flux.flux.messenger.dto.MessageStatusUpdate;
import ru.flux.flux.messenger.MessageStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToChat(UUID chatId, MessageResponse message) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                message
        );
    }

    public void sendReadStatus(UUID chatId, UUID readByUserId) {
        MessageStatusUpdate update = new MessageStatusUpdate(
                chatId, null, MessageStatus.READ
        );
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId + "/status",
                update
        );
    }

    public void sendDeleteEvent(UUID chatId, UUID messageId) {
        MessageStatusUpdate update = new MessageStatusUpdate(chatId, messageId, null);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId + "/delete",
                update
        );
    }
}