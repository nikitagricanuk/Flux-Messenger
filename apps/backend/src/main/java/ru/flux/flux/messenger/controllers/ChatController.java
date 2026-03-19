package ru.flux.flux.messenger.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.services.ChatService;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatResponse> getAllChats() {
        return chatService.getAllChats();
    }

    @GetMapping("/{id}")
    public ChatResponse getChatById(@PathVariable UUID id) {
        return chatService.getChatById(id);
    }

    @PostMapping
    public ChatResponse createChat(@RequestBody CreateChatRequest request) {
        return chatService.createChat(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChatById(@PathVariable UUID id) {
        chatService.deleteChatById(id);
    }
}
