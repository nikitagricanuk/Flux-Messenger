package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PublicKeyCredential response from navigator.credentials.create(). Binary fields are base64url-encoded.")
public class PasskeyRegistrationFinishRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String rawId;

    private String type;

    private Response response;

    private List<String> transports;

    @Data
    public static class Response {
        @NotBlank
        private String clientDataJSON;

        @NotBlank
        private String attestationObject;
    }
}
