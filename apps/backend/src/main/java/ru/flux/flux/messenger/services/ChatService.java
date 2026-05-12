package ru.flux.flux.messenger.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.Chat;
import ru.flux.flux.messenger.ChatType;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;
import ru.flux.flux.messenger.exceptions.ChatAlreadyExistsException;
import ru.flux.flux.messenger.exceptions.ChatNotFoundException;
import ru.flux.flux.messenger.repositories.ChatRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private final ChatRepository repository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getAllChats(UUID currentUserId) {
        return repository.findAll()
                .stream()
                .map(chat -> toResponse(chat, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatResponse getChatById(UUID id, UUID currentUserId) {
        Chat chat = repository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException(id));
        return toResponse(chat, currentUserId);
    }

    @Transactional
    public ChatResponse createChat(CreateChatRequest request, UUID currentUserId) {
        if (request.type() == ChatType.DIRECT) {
            boolean exists = !repository.findDirectChatWithExactMembers(
                    request.memberIds(), request.memberIds().size()
            ).isEmpty();
            if (exists) {
                throw new ChatAlreadyExistsException("A direct chat between these users already exists");
            }
        }

        if (request.memberIds().size() != 2 ||
                request.memberIds().get(0).equals(request.memberIds().get(1))) {
            log.debug("Direct chat requires exactly 2 distinct members");
            throw new IllegalArgumentException("Direct chat requires exactly 2 distinct members");
        }

        Chat chat = new Chat();
        chat.setType(request.type());
        chat.setName(request.name());
        chat.setAvatarUrl(request.avatarUrl());
        request.memberIds().forEach(memberId -> userRepository.findById(memberId).ifPresent(chat::addMember));
        return toResponse(repository.save(chat), currentUserId);
    }

    @Transactional
    public void deleteChatById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ChatNotFoundException(id);
        }
        repository.deleteById(id);
        log.debug("Deleted user with ID = {}", id);
    }

    private ChatResponse toResponse(Chat chat, UUID currentUserId) {
        String chatName = chat.getName();
        String avatarUrl = chat.getAvatarUrl();

        if (chat.getType() == ChatType.DIRECT) {
            UUID opponentId = chat.getMemberIds().stream()
                    .filter(id -> !id.equals(currentUserId))
                    .findFirst()
                    .orElse(currentUserId);

            User opponent = userRepository.findById(opponentId).orElse(null);
            if (opponent != null) {
                chatName = opponent.getLastName() != null
                        ? opponent.getFirstName() + " " + opponent.getLastName()
                        : opponent.getFirstName();
                avatarUrl = opponent.getAvatarUrl();
            }
        }

        return new ChatResponse(
                chat.getId(),
                chatName,
                avatarUrl,
                chat.getType(),
                List.copyOf(chat.getMemberIds()),
                "Hey, how are you?",
                LocalDateTime.now().minusHours(1)
        );
    }
}
