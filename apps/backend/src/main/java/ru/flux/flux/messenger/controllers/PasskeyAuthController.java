package ru.flux.flux.messenger.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.PasskeyOptionsRequest;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.PasskeyService;

@RestController
@RequestMapping("/api/auth/passkey")
@RequiredArgsConstructor
public class PasskeyAuthController {

    private final PasskeyService passkeyService;
    private final JwtService jwtService;

    @PostMapping("/options")
    public ResponseEntity<PublicKeyCredentialCreationOptions> options(
            @Valid @RequestBody PasskeyOptionsRequest request
    ) {
        PasskeyService.PasskeyOptions result = passkeyService.startPasskey(request.phone());
        return ResponseEntity.ok()
                .header("X-Challenge-Nonce", result.nonce())
                .body(result.options());
    }

    @PostMapping("/complete")
    public ResponseEntity<JwtAuthenticationResponse> complete(
            @RequestHeader("X-Challenge-Nonce") String nonce,
            @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse> credential
    ) {
        User user = passkeyService.completePasskey(nonce, credential);
        return ResponseEntity.ok(JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build());
    }
}