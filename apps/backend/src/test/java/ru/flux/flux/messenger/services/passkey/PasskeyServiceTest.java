package ru.flux.flux.messenger.services.passkey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.UserPasskey;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyAuthenticationStartResponse;
import ru.flux.flux.messenger.dto.PasskeyRegistrationFinishRequest;
import ru.flux.flux.messenger.dto.PasskeyRegistrationStartResponse;
import ru.flux.flux.messenger.exceptions.PasskeyVerificationException;
import ru.flux.flux.messenger.repositories.UserPasskeyRepository;
import ru.flux.flux.messenger.services.JwtService;
import ru.flux.flux.messenger.services.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasskeyServiceTest {

    @Mock
    private PasskeyVerifier verifier;

    @Mock
    private UserPasskeyRepository passkeyRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    private ChallengeStore challengeStore;
    private PasskeyService service;
    private User user;

    @BeforeEach
    void setUp() {
        challengeStore = new ChallengeStore();
        service = new PasskeyService(verifier, challengeStore, passkeyRepository, userService, jwtService);
        ReflectionTestUtils.setField(service, "rpId", "localhost");
        ReflectionTestUtils.setField(service, "rpName", "Flux");
        user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice").username("alice")
                .phone("+12345678901").password("x").notifications(true).build();
        lenient().when(passkeyRepository.findAllByUserId(any())).thenReturn(List.of());
    }

    @Test
    void startRegistrationReturnsFreshChallenge() {
        PasskeyRegistrationStartResponse r1 = service.startRegistration(user);
        PasskeyRegistrationStartResponse r2 = service.startRegistration(user);

        assertThat(r1.getChallenge()).isNotBlank();
        assertThat(r2.getChallenge()).isNotEqualTo(r1.getChallenge());
        assertThat(r1.getRp().getId()).isEqualTo("localhost");
        assertThat(r1.getUser().getId()).isNotBlank();
        assertThat(r1.getPubKeyCredParams()).extracting(PasskeyRegistrationStartResponse.PubKeyCredParam::getAlg)
                .contains(-7L, -257L);
        assertThat(r1.getAuthenticatorSelection().getResidentKey()).isEqualTo("required");
    }

    @Test
    void finishRegistrationVerifiesAndPersists() {
        PasskeyRegistrationStartResponse start = service.startRegistration(user);
        byte[] challenge = Base64.getUrlDecoder().decode(start.getChallenge());
        String clientDataJSON = "{\"type\":\"webauthn.create\",\"challenge\":\"" + start.getChallenge()
                + "\",\"origin\":\"http://localhost:8080\"}";
        byte[] clientDataBytes = clientDataJSON.getBytes(StandardCharsets.UTF_8);
        byte[] attestationObject = new byte[]{1, 2, 3};
        byte[] credentialId = new byte[]{9, 9};
        byte[] pubKey = new byte[]{5, 5};
        when(verifier.verifyRegistration(any(), any(), any()))
                .thenReturn(new PasskeyVerifier.RegistrationResult(credentialId, pubKey, 1L, null));

        PasskeyRegistrationFinishRequest req = new PasskeyRegistrationFinishRequest();
        req.setId("x");
        req.setRawId("x");
        req.setType("public-key");
        PasskeyRegistrationFinishRequest.Response resp = new PasskeyRegistrationFinishRequest.Response();
        resp.setClientDataJSON(Base64.getUrlEncoder().withoutPadding().encodeToString(clientDataBytes));
        resp.setAttestationObject(Base64.getUrlEncoder().withoutPadding().encodeToString(attestationObject));
        req.setResponse(resp);

        service.finishRegistration(user, req);

        org.mockito.ArgumentCaptor<UserPasskey> captor = org.mockito.ArgumentCaptor.forClass(UserPasskey.class);
        org.mockito.Mockito.verify(passkeyRepository).save(captor.capture());
        UserPasskey saved = captor.getValue();
        assertThat(saved.getCredentialId()).isEqualTo(credentialId);
        assertThat(saved.getPublicKeyCose()).isEqualTo(pubKey);
        assertThat(saved.getSignCount()).isEqualTo(1L);
        assertThat(saved.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void finishRegistrationRejectsUnknownChallenge() {
        String clientDataJSON = "{\"type\":\"webauthn.create\",\"challenge\":\"AAAA\",\"origin\":\"x\"}";
        PasskeyRegistrationFinishRequest req = new PasskeyRegistrationFinishRequest();
        req.setId("x");
        req.setRawId("x");
        PasskeyRegistrationFinishRequest.Response resp = new PasskeyRegistrationFinishRequest.Response();
        resp.setClientDataJSON(Base64.getUrlEncoder().withoutPadding()
                .encodeToString(clientDataJSON.getBytes(StandardCharsets.UTF_8)));
        resp.setAttestationObject("AQID");
        req.setResponse(resp);

        assertThatThrownBy(() -> service.finishRegistration(user, req))
                .isInstanceOf(PasskeyVerificationException.class)
                .hasMessageContaining("Challenge");
    }

    @Test
    void startAuthenticationStoresChallenge() {
        PasskeyAuthenticationStartResponse response = service.startAuthentication();
        assertThat(response.getChallenge()).isNotBlank();
        assertThat(response.getRpId()).isEqualTo("localhost");
        assertThat(response.getUserVerification()).isEqualTo("preferred");

        byte[] raw = Base64.getUrlDecoder().decode(response.getChallenge());
        ChallengeContext ctx = challengeStore.peek(raw);
        assertThat(ctx).isNotNull();
        assertThat(ctx.kind()).isEqualTo(ChallengeContext.Kind.AUTHENTICATION);
    }

    @Test
    void finishAuthenticationReturnsJwt() {
        PasskeyAuthenticationStartResponse start = service.startAuthentication();
        String challengeB64 = start.getChallenge();
        String clientDataJSON = "{\"type\":\"webauthn.get\",\"challenge\":\"" + challengeB64
                + "\",\"origin\":\"http://localhost:8080\"}";

        byte[] credentialId = new byte[]{7, 7};
        UserPasskey stored = UserPasskey.builder()
                .credentialId(credentialId)
                .publicKeyCose(new byte[]{5, 5})
                .signCount(3L)
                .userId(user.getId())
                .build();
        when(passkeyRepository.findByCredentialId(any())).thenReturn(Optional.of(stored));
        when(verifier.verifyAuthentication(any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new PasskeyVerifier.AuthenticationResult(4L));
        when(userService.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh");

        PasskeyAuthenticationFinishRequest req = new PasskeyAuthenticationFinishRequest();
        req.setId("x");
        req.setRawId(Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        req.setType("public-key");
        PasskeyAuthenticationFinishRequest.Response resp = new PasskeyAuthenticationFinishRequest.Response();
        resp.setClientDataJSON(Base64.getUrlEncoder().withoutPadding()
                .encodeToString(clientDataJSON.getBytes(StandardCharsets.UTF_8)));
        resp.setAuthenticatorData("AQID");
        resp.setSignature("BAUG");
        req.setResponse(resp);

        JwtAuthenticationResponse response = service.finishAuthentication(req);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(stored.getSignCount()).isEqualTo(4L);
    }

    @Test
    void finishAuthenticationRejectsRegressedSignCount() {
        PasskeyAuthenticationStartResponse start = service.startAuthentication();
        String challengeB64 = start.getChallenge();
        String clientDataJSON = "{\"type\":\"webauthn.get\",\"challenge\":\"" + challengeB64
                + "\",\"origin\":\"x\"}";

        byte[] credentialId = new byte[]{7};
        UserPasskey stored = UserPasskey.builder()
                .credentialId(credentialId).publicKeyCose(new byte[]{5})
                .signCount(10L).userId(user.getId()).build();
        when(passkeyRepository.findByCredentialId(any())).thenReturn(Optional.of(stored));
        when(verifier.verifyAuthentication(any(), any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(new PasskeyVerifier.AuthenticationResult(5L));

        PasskeyAuthenticationFinishRequest req = new PasskeyAuthenticationFinishRequest();
        req.setRawId(Base64.getUrlEncoder().withoutPadding().encodeToString(credentialId));
        PasskeyAuthenticationFinishRequest.Response resp = new PasskeyAuthenticationFinishRequest.Response();
        resp.setClientDataJSON(Base64.getUrlEncoder().withoutPadding()
                .encodeToString(clientDataJSON.getBytes(StandardCharsets.UTF_8)));
        resp.setAuthenticatorData("AQID");
        resp.setSignature("BAUG");
        req.setResponse(resp);
        req.setId("x");

        assertThatThrownBy(() -> service.finishAuthentication(req))
                .isInstanceOf(PasskeyVerificationException.class)
                .hasMessageContaining("sign count");
    }
}
