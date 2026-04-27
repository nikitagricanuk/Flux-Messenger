package ru.flux.flux.messenger.controllers;

import java.security.Principal;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.SendMessageRequest;
import ru.flux.flux.messenger.services.MessageService;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload SendMessageRequest request,
            Principal principal) {

        if (!(principal instanceof Authentication authentication) ||
                !(authentication.getPrincipal() instanceof User user)) {
            throw new SecurityException("Unauthenticated WebSocket user");
        }

        messageService.sendMessage(request, user.getId());
    }
}
