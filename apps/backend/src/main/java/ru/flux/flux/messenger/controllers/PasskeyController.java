package ru.flux.flux.messenger.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationStartResponse;
import ru.flux.flux.messenger.dto.PasskeyRegistrationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyRegistrationStartResponse;
import ru.flux.flux.messenger.services.passkey.PasskeyService;

@Tag(name = "Passkey", description = "WebAuthn/FIDO2 passwordless sign-in")
@RestController
@RequestMapping("/api/auth/passkey")
@RequiredArgsConstructor
public class PasskeyController {

    private final PasskeyService passkeyService;

    @PostMapping("/register/start")
    @Operation(summary = "Start passkey registration (requires auth)")
    public ResponseEntity<PasskeyRegistrationStartResponse> registerStart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(passkeyService.startRegistration(user));
    }

    @PostMapping("/register/finish")
    @Operation(summary = "Finish passkey registration (requires auth)")
    public ResponseEntity<Void> registerFinish(@AuthenticationPrincipal User user,
                                               @RequestBody @Valid PasskeyRegistrationFinishRequest request) {
        passkeyService.finishRegistration(user, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/authenticate/start")
    @Operation(summary = "Start discoverable passkey authentication")
    public ResponseEntity<PasskeyAuthenticationStartResponse> authenticateStart() {
        return ResponseEntity.ok(passkeyService.startAuthentication());
    }

    @PostMapping("/authenticate/finish")
    @Operation(summary = "Finish passkey authentication and issue JWT")
    public ResponseEntity<JwtAuthenticationResponse> authenticateFinish(@RequestBody @Valid PasskeyAuthenticationFinishRequest request) {
        return ResponseEntity.ok(passkeyService.finishAuthentication(request));
    }
}
