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
    private static final String USER_PREFIX  = "/topic/user/";

    public void sendToChat(UUID chatId, MessageResponse message) {
        messagingTemplate.convertAndSend(
                USER_PREFIX  + chatId,
                message
        );
    }

    public void sendReadStatus(UUID chatId, UUID readByUserId) {
        MessageStatusUpdate update = new MessageStatusUpdate(
                chatId, null, MessageStatus.READ, readByUserId
        );
        messagingTemplate.convertAndSend(
                USER_PREFIX + chatId + "/status",
                update
        );
    }

    public void sendDeleteEvent(UUID chatId, UUID messageId) {
        MessageStatusUpdate update = new MessageStatusUpdate(chatId, messageId, null, null);
        messagingTemplate.convertAndSend(
                USER_PREFIX  + chatId + "/delete",
                update
        );
    }
}