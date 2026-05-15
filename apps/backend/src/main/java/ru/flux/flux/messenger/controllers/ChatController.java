package ru.flux.flux.messenger.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.AddFavoriteRequest;
import ru.flux.flux.messenger.dto.FavoriteResponse;
import ru.flux.flux.messenger.services.ChatService;
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

    @GetMapping("/favorites")
    public List<FavoriteResponse> getFavorites(@AuthenticationPrincipal User currentUser) {
        return chatService.getFavorites(currentUser.getId());
    }

    @GetMapping("/{id}")
    public ChatResponse getChatById(@PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        return chatService.getChatById(id, currentUser.getId());
    }

    @PostMapping("/direct")
    public ChatResponse createDirectChat(@Valid @RequestBody CreateChatRequest request, @AuthenticationPrincipal User currentUser) {
        return chatService.createChat(request, currentUser.getId());
    }

    @PostMapping("/group")
    public ChatResponse createGroupChat(
            @RequestParam String name,
            @RequestParam List<UUID> memberIds,
            @RequestParam(required = false) MultipartFile avatar,
            @AuthenticationPrincipal User currentUser) {
        return chatService.createGroupChat(name, memberIds, avatar, currentUser.getId());
    }

    @PostMapping("/favorites")
    public FavoriteResponse createFavorite(@RequestBody AddFavoriteRequest request, @AuthenticationPrincipal User currentUser) {
        return chatService.addFavorite(request, currentUser.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChatById(@PathVariable UUID id) {
        chatService.deleteChatById(id);
    }
}
