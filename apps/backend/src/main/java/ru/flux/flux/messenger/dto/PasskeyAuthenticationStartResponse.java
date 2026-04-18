package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PublicKeyCredentialRequestOptions for passkey authentication. Challenge is base64url-encoded.")
public class PasskeyAuthenticationStartResponse {
    private String challenge;
    private String rpId;
    private long timeout;
    private String userVerification;
}
