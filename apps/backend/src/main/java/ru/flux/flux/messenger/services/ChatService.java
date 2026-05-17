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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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