package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "PublicKeyCredential response from navigator.credentials.get(). Binary fields are base64url-encoded.")
public class PasskeyAuthenticationFinishRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String rawId;

    private String type;

    private Response response;

    @Data
    public static class Response {
        @NotBlank
        private String clientDataJSON;

        @NotBlank
        private String authenticatorData;

        @NotBlank
        private String signature;

        private String userHandle;
    }
}
