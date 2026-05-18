package ru.flux.flux.messenger.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.ChatType;
import ru.flux.flux.messenger.Message;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.AddFavoriteRequest;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;
import ru.flux.flux.messenger.dto.FavoriteResponse;
import ru.flux.flux.messenger.exceptions.ChatNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.MessageRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import ru.flux.flux.messenger.dto.UserResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private final ChatRepository repository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final StorageService storageService;

    public ChatService(ChatRepository repository,
                       UserRepository userRepository,
                       MessageRepository messageRepository,
                       StorageService storageService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getAllChats(UUID currentUserId) {
        List<UUID> chatIds = repository.findChatIdsByMemberId(currentUserId);
        if (chatIds.isEmpty()) return List.of();
        // Second query loads ALL members of each chat without the WHERE filtering the fetched collection
        List<Chat> chats = repository.findByIdsWithAllMembers(chatIds);

        // Batch-load peer users for all DIRECT chats in one query
        List<UUID> peerIds = chats.stream()
                .filter(c -> c.getType() == ChatType.DIRECT)
                .flatMap(c -> c.getMemberIds().stream().filter(id -> !id.equals(currentUserId)))
                .distinct()
                .toList();
        Map<UUID, User> peerMap = peerIds.isEmpty() ? Map.of()
                : userRepository.findAllById(peerIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        // Batch-load last message for each chat in one query (reuse chatIds already resolved above)
        Map<UUID, Message> lastMessages = messageRepository.findLastMessagesForChats(chatIds)
                .stream().collect(Collectors.toMap(m -> m.getChat().getId(), m -> m));

        return chats.stream()
                .map(chat -> toResponse(chat, currentUserId, peerMap, lastMessages))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getChatMembers(UUID chatId, UUID currentUserId) {
        Chat chat = repository.findWithMembersById(chatId)
                .orElseThrow(() -> new ChatNotFoundException(chatId));
        if (!chat.getMemberIds().contains(currentUserId)) {
            throw new SecurityException("User is not a member of this chat");
        }
        return chat.getMembers().stream()
                .map(cm -> {
                    User u = cm.getUser();
                    return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(),
                            u.getHandle(), u.getDateOfBirth(), u.getPhone(), u.getEmail(),
                            u.getAvatarUrl(), u.isNotifications(), u.getBio());
                })
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
        if (request.type() == ChatType.DIRECT &&
                (request.memberIds().size() != 2 ||
                request.memberIds().get(0).equals(request.memberIds().get(1)))) {
            throw new IllegalArgumentException("Direct chat requires exactly 2 distinct members");
        }

        List<UUID> members = new ArrayList<>(request.memberIds());
        if (!members.contains(currentUserId)) {
            members.add(currentUserId);
        }

        if (request.type() == ChatType.DIRECT) {
            List<Chat> existing = repository.findDirectChatWithExactMembers(
                    members, members.size()
            );
            if (!existing.isEmpty()) {
                return toResponse(existing.get(0), currentUserId);
            }
        }

        Chat chat = new Chat();
        chat.setType(request.type());
        chat.setName(request.name());
        chat.setAvatarUrl(request.avatarUrl());
        members.forEach(memberId -> userRepository.findById(memberId).ifPresent(chat::addMember));
        return toResponse(repository.save(chat), currentUserId);
    }

    @Transactional
    public ChatResponse createGroupChat(String name, List<UUID> memberIds, MultipartFile avatar, UUID currentUserId) {
        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setName(name);
        memberIds.forEach(id -> userRepository.findById(id).ifPresent(chat::addMember));

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String ext = StringUtils.getFilenameExtension(avatar.getOriginalFilename());
                String objectName = "chat-" + UUID.randomUUID() + (ext != null ? "." + ext : "");
                String url = storageService.upload(objectName, avatar.getInputStream(), avatar.getSize(), avatar.getContentType());
                chat.setAvatarUrl(url);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload chat avatar", e);
            }
        }

        return toResponse(repository.save(chat), currentUserId);
    }

    @Transactional
    public void deleteChatById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ChatNotFoundException(id);
        }
        repository.deleteById(id);
        log.debug("Deleted chat with ID = {}", id);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getFavorites().stream()
                .map(chat -> toFavoriteResponse(chat, userId))
                .toList();
    }

    @Transactional
    public FavoriteResponse addFavorite(AddFavoriteRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Chat target = repository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("Target chat not found"));
        user.addFavorite(target);
        userRepository.save(user);
        return toFavoriteResponse(target, userId);
    }

    private FavoriteResponse toFavoriteResponse(Chat chat, UUID currentUserId) {
        ChatResponse r = toResponse(chat, currentUserId);
        return new FavoriteResponse(r.id(), r.name(), r.profilePicture());
    }

    private ChatResponse toResponse(Chat chat, UUID currentUserId) {
        return toResponse(chat, currentUserId, null, null);
    }

    private ChatResponse toResponse(Chat chat, UUID currentUserId,
                                     Map<UUID, User> peerMap,
                                     Map<UUID, Message> lastMessages) {
        String name = chat.getName();
        String profilePicture = chat.getAvatarUrl();

        if (chat.getType() == ChatType.DIRECT) {
            UUID peerId = chat.getMemberIds().stream()
                    .filter(memberId -> !memberId.equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (peerId != null) {
                User peer = peerMap != null ? peerMap.get(peerId)
                        : userRepository.findById(peerId).orElse(null);
                if (peer != null) {
                    String lastName = peer.getLastName();
                    name = (lastName != null && !lastName.isBlank())
                            ? peer.getFirstName() + " " + lastName
                            : peer.getFirstName();
                    profilePicture = peer.getAvatarUrl();
                }
            }

            if (name == null || name.isBlank()) {
                name = "Удалённый аккаунт";
            }
        }

        Message lastMessageEntity = lastMessages != null ? lastMessages.get(chat.getId())
                : messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.getId());
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