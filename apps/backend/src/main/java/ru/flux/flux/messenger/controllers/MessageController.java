package ru.flux.flux.messenger.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.MessageResponse;
import ru.flux.flux.messenger.dto.SendMessageRequest;
import ru.flux.flux.messenger.services.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;


import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(messageService.sendMessage(request, user.getId()));
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageResponse>> getHistory(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getMessages(chatId, user.getId(), page, size));
    }

    @PostMapping("/chat/{chatId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal User user) {
        messageService.markAsRead(chatId, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable UUID messageId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(messageService.editMessage(messageId, request.text(), user.getId()));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal User user) {
        messageService.deleteMessage(messageId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal User user) {
        String url = messageService.uploadMedia(file);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/chat/{chatId}/media")
    public ResponseEntity<List<MessageResponse>> getMedia(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(messageService.getMediaMessages(chatId, user.getId()));
    }
}