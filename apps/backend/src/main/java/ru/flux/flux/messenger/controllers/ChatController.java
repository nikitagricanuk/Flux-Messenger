package ru.flux.flux.messenger.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.services.ChatService;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.ChatResponse;
import ru.flux.flux.messenger.dto.CreateChatRequest;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatResponse> getAllChats(@AuthenticationPrincipal User currentUser) {
        return chatService.getAllChats(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ChatResponse getChatById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        return chatService.getChatById(id, currentUser.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChatById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        chatService.deleteChatById(id);
    }

    @PostMapping
    public ChatResponse createChat(
            @Valid @RequestBody CreateChatRequest request,
            @AuthenticationPrincipal User currentUser) {
        return chatService.createOrGetDirect(request, currentUser.getId());
    }
}
