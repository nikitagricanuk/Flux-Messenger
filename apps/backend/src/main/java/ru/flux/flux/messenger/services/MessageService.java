package ru.flux.flux.messenger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.MessageStatus;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.MessageResponse;
import ru.flux.flux.messenger.dto.SendMessageRequest;
import ru.flux.flux.messenger.exceptions.ChatNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.MessageRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final StorageService storageService;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, UUID senderId) {
        Chat chat = requireChatMember(request.chatId(), senderId);

        if (!chat.getMemberIds().contains(senderId)) {
            throw new SecurityException("User is not a member of this chat");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .text(request.text() != null ? request.text() : "")
                .mediaUrl(request.mediaUrl())
                .mediaType(request.mediaType())
                .build();

        message = messageRepository.save(message);
        MessageResponse response = MessageResponse.from(message);

        webSocketService.sendToChat(chat.getId(), response);

        return response;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID chatId, UUID userId, int page, int size) {
        requireChatMember(chatId, userId);

        Page<Message> messages = messageRepository
                .findByChatIdOrderByCreatedAtAsc(chatId, PageRequest.of(page, size));

        return messages.stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(UUID chatId, UUID userId) {
        requireChatMember(chatId, userId);
        messageRepository.updateStatusForChat(chatId, userId, MessageStatus.READ);
        webSocketService.sendReadStatus(chatId, userId);
    }

    private Chat requireChatMember(UUID chatId, UUID userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        if (!chat.getMemberIds().contains(userId)) {
            throw new SecurityException("User is not a member of this chat");
        }

        return chat;
    }

    @Transactional
    public MessageResponse editMessage(UUID messageId, String newText, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new SecurityException("Cannot edit someone else's message");
        }

        message.setText(newText);
        message = messageRepository.save(message);

        webSocketService.sendToChat(message.getChat().getId(), MessageResponse.from(message));

        return MessageResponse.from(message);
    }

    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new SecurityException("Cannot delete someone else's message");
        }

        UUID chatId = message.getChat().getId();
        messageRepository.delete(message);

        webSocketService.sendDeleteEvent(chatId, messageId);
    }

    @Transactional
    public String uploadMedia(MultipartFile file) {
        try {
            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String objectName = "media/msg-" + UUID.randomUUID() 
                    + (ext != null ? "." + ext : "");
            return storageService.upload(
                    objectName,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload media", e);
        }
    }

    @Transactional
    public List<MessageResponse> getMediaMessages(UUID chatId, UUID userId) {
        requireChatMember(chatId, userId);
        return messageRepository.findByChatIdAndMediaUrlNotNull(chatId)
                .stream()
                .map(MessageResponse::from)
                .toList();
    }
}