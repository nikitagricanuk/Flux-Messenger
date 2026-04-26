package ru.flux.flux.messenger.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.ChatType;
import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;
import ru.flux.flux.messenger.exceptions.ChatNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.MessageRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private final ChatRepository repository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ChatService(ChatRepository repository,
                       UserRepository userRepository,
                       MessageRepository messageRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getAllChats(UUID currentUserId) {
        return repository.findAll()
                .stream()
                .filter(chat -> chat.getMemberIds().contains(currentUserId))
                .map(chat -> toResponse(chat, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatResponse getChatById(UUID id, UUID currentUserId) {
        Chat chat = requireChatMember(id, currentUserId);
        return toResponse(chat, currentUserId);
    }

    private Chat requireChatMember(UUID chatId, UUID userId) {
        Chat chat = repository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));

        if (!chat.getMemberIds().contains(userId)) {
            throw new SecurityException("User is not a member of this chat");
        }

        return chat;
    }

    @Transactional
    public ChatResponse createOrGetDirect(CreateChatRequest request, UUID currentUserId) {
        List<UUID> members = new ArrayList<>(request.memberIds());
        if (!members.contains(currentUserId)) {
            members.add(currentUserId);
        }

        if (request.type() == ChatType.DIRECT) {
            List<Chat> existing = repository.findByTypeAndExactMembers(
                    ChatType.DIRECT.name(), members, members.size()
            );
            if (!existing.isEmpty()) {
                return toResponse(existing.get(0), currentUserId);
            }
        }

        Chat chat = new Chat(request.type(), request.name(), members);
        chat.setAvatarUrl(request.avatarUrl());
        return toResponse(repository.save(chat), currentUserId);
    }

    @Transactional
    public void deleteChatById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ChatNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private ChatResponse toResponse(Chat chat, UUID currentUserId) {
        String name = chat.getName();
        String profilePicture = chat.getAvatarUrl();

        if (chat.getType() == ChatType.DIRECT) {
            UUID peerId = chat.getMemberIds().stream()
                    .filter(memberId -> !memberId.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (peerId != null) {
                User peer = userRepository.findById(peerId).orElse(null);
                if (peer != null) {
                    String lastName = peer.getLastName();
                    name = (lastName != null && !lastName.isBlank())
                            ? peer.getFirstName() + " " + lastName
                            : peer.getFirstName();
                    profilePicture = peer.getAvatarUrl();
                }
            }

            if (name == null || name.isBlank()) {
                name = "Личный чат";
            }
        }

        Message lastMessageEntity = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.getId());
        String lastMessage = lastMessageEntity != null ? lastMessageEntity.getText() : "";
        LocalDateTime lastMessageAt = lastMessageEntity != null
                ? LocalDateTime.ofInstant(lastMessageEntity.getCreatedAt(), ZoneId.systemDefault())
                : null;

        return new ChatResponse(
                chat.getId(),
                name,
                profilePicture,
                chat.getType(),
                List.copyOf(chat.getMemberIds()),
                lastMessage,
                lastMessageAt
        );
    }
}
