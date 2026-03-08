package ru.flux.flux.messenger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;
import ru.flux.flux.messenger.exceptions.ChatNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private final ChatRepository repository;

    public ChatService(ChatRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getAllChats() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatResponse getChatById(UUID id) {
        Chat chat = repository.findById(id)
                .orElseThrow(() -> new ChatNotFoundException(id));
        return toResponse(chat);
    }

    @Transactional
    public ChatResponse createChat(CreateChatRequest request) {
        Chat chat = new Chat();
        chat.setName(request.name());
        chat.setType(request.type());
        chat.setMemberIds(request.memberIds());

        Chat saved = repository.save(chat);
        return toResponse(saved);
    }

    @Transactional
    public void deleteChatById(UUID id) {
        if (!repository.existsById(id)) {
            throw new ChatNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private ChatResponse toResponse(Chat chat) {
        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                List.copyOf(chat.getMemberIds())
        );
    }
}
