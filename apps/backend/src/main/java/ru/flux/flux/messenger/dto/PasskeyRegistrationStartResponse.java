package ru.flux.flux.messenger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PublicKeyCredentialCreationOptions for passkey registration. All binary fields are base64url-encoded.")
public class PasskeyRegistrationStartResponse {
    private RelyingParty rp;
    private UserInfo user;
    private String challenge;
    private List<PubKeyCredParam> pubKeyCredParams;
    private long timeout;
    private String attestation;
    private AuthenticatorSelection authenticatorSelection;
    private List<Descriptor> excludeCredentials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelyingParty {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String name;
        private String displayName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PubKeyCredParam {
        private String type;
        private long alg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticatorSelection {
        private String authenticatorAttachment;
        private boolean requireResidentKey;
        private String residentKey;
        private String userVerification;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Descriptor {
        private String type;
        private String id;
        private List<String> transports;
    }
}
