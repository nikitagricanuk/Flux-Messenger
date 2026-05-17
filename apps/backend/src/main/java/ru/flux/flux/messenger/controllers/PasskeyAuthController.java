package ru.flux.flux.messenger.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.web.bind.annotation.*;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.PasskeyAssertionOptionsResponse;
import ru.flux.flux.messenger.dto.PasskeyCreationOptionsResponse;
import ru.flux.flux.messenger.dto.PasskeyOptionsRequest;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.PasskeyService;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/auth/passkey")
@RequiredArgsConstructor
public class PasskeyAuthController {

    private final PasskeyService passkeyService;
    private final JwtService jwtService;

    @PostMapping("/options")
    public ResponseEntity<PasskeyCreationOptionsResponse> options(
            @Valid @RequestBody PasskeyOptionsRequest request
    ) {
        PasskeyService.PasskeyOptions result = passkeyService.startPasskey(request.phone());
        return ResponseEntity.ok()
                .header("X-Challenge-Nonce", result.nonce())
                .body(toCreationResponse(result.options()));
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

    @PostMapping("/authenticate/start")
    public ResponseEntity<PasskeyAssertionOptionsResponse> authenticateStart() {
        PasskeyService.AuthOptions result = passkeyService.startAuthentication();
        return ResponseEntity.ok()
                .header("X-Challenge-Nonce", result.nonce())
                .body(toAssertionResponse(result.options()));
    }

    @PostMapping("/authenticate/finish")
    public ResponseEntity<JwtAuthenticationResponse> authenticateFinish(
            @RequestHeader("X-Challenge-Nonce") String nonce,
            @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse> credential
    ) {
        User user = passkeyService.completeAuthentication(nonce, credential);
        return ResponseEntity.ok(JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build());
    }

    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();

    private PasskeyCreationOptionsResponse toCreationResponse(PublicKeyCredentialCreationOptions o) {
        String challenge = BASE64URL.encodeToString(o.getChallenge().getBytes());
        String userId    = BASE64URL.encodeToString(o.getUser().getId().getBytes());

        var rp   = new PasskeyCreationOptionsResponse.Rp(o.getRp().getId(), o.getRp().getName());
        var user = new PasskeyCreationOptionsResponse.User(userId, o.getUser().getName(), o.getUser().getDisplayName());
        var params = o.getPubKeyCredParams().stream()
                .map(p -> new PasskeyCreationOptionsResponse.PubKeyCredParam("public-key", p.getAlg().getValue()))
                .toList();
        String residentKeyValue = o.getAuthenticatorSelection().getResidentKey().getValue();
        var authSel = new PasskeyCreationOptionsResponse.AuthenticatorSelection(
                "platform",
                "required".equals(residentKeyValue),
                residentKeyValue,
                o.getAuthenticatorSelection().getUserVerification().getValue());

        return new PasskeyCreationOptionsResponse(
                challenge, rp, user, params,
                o.getTimeout().toMillis(),
                "none", List.of(), authSel);
    }

    private PasskeyAssertionOptionsResponse toAssertionResponse(PublicKeyCredentialRequestOptions o) {
        String challenge = BASE64URL.encodeToString(o.getChallenge().getBytes());
        return new PasskeyAssertionOptionsResponse(
                challenge, o.getRpId(),
                o.getTimeout().toMillis(),
                o.getUserVerification().getValue(),
                List.of());
    }
}