package ru.flux.flux.messenger.services.passkey;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.UserPasskey;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationStartResponse;
import ru.flux.flux.messenger.dto.PasskeyRegistrationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyRegistrationStartResponse;
import ru.flux.flux.messenger.exceptions.PasskeyVerificationException;
import ru.flux.flux.messenger.exceptions.UserNotFoundException;
import ru.flux.flux.messenger.repositories.UserPasskeyRepository;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasskeyService {

    private static final long TIMEOUT_MS = 60_000L;
    private static final String PUB_KEY_TYPE = "public-key";

    private final PasskeyVerifier verifier;
    private final ChallengeStore challengeStore;
    private final UserPasskeyRepository passkeyRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @Value("${webauthn.rp-id:localhost}")
    private String rpId;

    @Value("${webauthn.rp-name:Flux Messenger}")
    private String rpName;

    public PasskeyRegistrationStartResponse startRegistration(User user) {
        byte[] challenge = challengeStore.generateChallenge();
        challengeStore.store(challenge, new ChallengeContext(challenge, user.getId(), ChallengeContext.Kind.REGISTRATION));

        List<PasskeyRegistrationStartResponse.Descriptor> excludeCredentials = passkeyRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(pk -> PasskeyRegistrationStartResponse.Descriptor.builder()
                        .type(PUB_KEY_TYPE)
                        .id(encodeB64Url(pk.getCredentialId()))
                        .transports(splitTransports(pk.getTransports()))
                        .build())
                .toList();

        return PasskeyRegistrationStartResponse.builder()
                .rp(PasskeyRegistrationStartResponse.RelyingParty.builder().id(rpId).name(rpName).build())
                .user(PasskeyRegistrationStartResponse.UserInfo.builder()
                        .id(encodeB64Url(user.getId().toString().getBytes(StandardCharsets.UTF_8)))
                        .name(user.getPhone() != null ? user.getPhone() : user.getHandle())
                        .displayName(firstNonBlank(user.getFirstName(), user.getHandle()))
                        .build())
                .challenge(encodeB64Url(challenge))
                .pubKeyCredParams(List.of(
                        PasskeyRegistrationStartResponse.PubKeyCredParam.builder().type(PUB_KEY_TYPE).alg(-7L).build(),
                        PasskeyRegistrationStartResponse.PubKeyCredParam.builder().type(PUB_KEY_TYPE).alg(-257L).build()
                ))
                .timeout(TIMEOUT_MS)
                .attestation("none")
                .authenticatorSelection(PasskeyRegistrationStartResponse.AuthenticatorSelection.builder()
                        .requireResidentKey(true)
                        .residentKey("required")
                        .userVerification("preferred")
                        .build())
                .excludeCredentials(excludeCredentials)
                .build();
    }

    @Transactional
    public void finishRegistration(User user, PasskeyRegistrationFinishRequest request) {
        if (request.getResponse() == null) {
            throw new PasskeyVerificationException("Missing response payload");
        }
        byte[] clientDataJSON = decodeB64Url(request.getResponse().getClientDataJSON());
        byte[] attestationObject = decodeB64Url(request.getResponse().getAttestationObject());

        byte[] challenge = extractChallenge(clientDataJSON);
        ChallengeContext ctx = challengeStore.consume(challenge);
        if (ctx == null) {
            throw new PasskeyVerificationException("Challenge not found or expired");
        }
        if (ctx.kind() != ChallengeContext.Kind.REGISTRATION) {
            throw new PasskeyVerificationException("Challenge is not a registration challenge");
        }
        if (ctx.userId() == null || !ctx.userId().equals(user.getId())) {
            throw new PasskeyVerificationException("Challenge does not belong to this user");
        }

        PasskeyVerifier.RegistrationResult result = verifier.verifyRegistration(challenge, clientDataJSON, attestationObject);

        UserPasskey passkey = UserPasskey.builder()
                .credentialId(result.credentialId())
                .publicKeyCose(result.publicKeyCose())
                .signCount(result.signCount())
                .aaguid(parseAaguid(result.aaguid()))
                .transports(joinTransports(request.getTransports()))
                .userId(user.getId())
                .build();

        passkeyRepository.save(passkey);
    }

    public PasskeyAuthenticationStartResponse startAuthentication() {
        byte[] challenge = challengeStore.generateChallenge();
        challengeStore.store(challenge, new ChallengeContext(challenge, null, ChallengeContext.Kind.AUTHENTICATION));

        return PasskeyAuthenticationStartResponse.builder()
                .challenge(encodeB64Url(challenge))
                .rpId(rpId)
                .timeout(TIMEOUT_MS)
                .userVerification("preferred")
                .build();
    }

    @Transactional
    public JwtAuthenticationResponse finishAuthentication(PasskeyAuthenticationFinishRequest request) {
        if (request.getResponse() == null) {
            throw new PasskeyVerificationException("Missing response payload");
        }
        byte[] clientDataJSON = decodeB64Url(request.getResponse().getClientDataJSON());
        byte[] authenticatorData = decodeB64Url(request.getResponse().getAuthenticatorData());
        byte[] signature = decodeB64Url(request.getResponse().getSignature());
        byte[] credentialId = decodeB64Url(request.getRawId());

        byte[] challenge = extractChallenge(clientDataJSON);
        ChallengeContext ctx = challengeStore.consume(challenge);
        if (ctx == null) {
            throw new PasskeyVerificationException("Challenge not found or expired");
        }
        if (ctx.kind() != ChallengeContext.Kind.AUTHENTICATION) {
            throw new PasskeyVerificationException("Challenge is not an authentication challenge");
        }

        UserPasskey stored = passkeyRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new PasskeyVerificationException("Unknown passkey credential"));

        PasskeyVerifier.AuthenticationResult result = verifier.verifyAuthentication(
                challenge,
                clientDataJSON,
                authenticatorData,
                signature,
                stored.getPublicKeyCose(),
                stored.getSignCount()
        );

        if (result.newSignCount() < stored.getSignCount()) {
            throw new PasskeyVerificationException("Invalid sign count (possible cloned authenticator)");
        }
        stored.setSignCount(result.newSignCount());
        stored.setLastUsedAt(Instant.now());
        passkeyRepository.save(stored);

        User user = userService.findById(stored.getUserId())
                .orElseThrow(() -> new UserNotFoundException(stored.getUserId()));

        return JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private static byte[] extractChallenge(byte[] clientDataJSON) {
        try {
            String json = new String(clientDataJSON, StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(json, Map.class);
            Object challenge = parsed.get("challenge");
            if (challenge == null) {
                throw new PasskeyVerificationException("clientDataJSON has no challenge");
            }
            return Base64.getUrlDecoder().decode(challenge.toString());
        } catch (PasskeyVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new PasskeyVerificationException("Invalid clientDataJSON: " + e.getMessage(), e);
        }
    }

    private static String encodeB64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] decodeB64Url(String value) {
        if (value == null) {
            throw new PasskeyVerificationException("Missing base64url field");
        }
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new PasskeyVerificationException("Invalid base64url value");
        }
    }

    private static List<String> splitTransports(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(","));
    }

    private static String joinTransports(List<String> transports) {
        if (transports == null || transports.isEmpty()) return null;
        return String.join(",", transports);
    }

    private static UUID parseAaguid(String aaguid) {
        if (aaguid == null || aaguid.isBlank()) return null;
        try {
            return UUID.fromString(aaguid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return "";
    }
}
