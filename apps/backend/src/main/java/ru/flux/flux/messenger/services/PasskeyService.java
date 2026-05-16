package ru.flux.flux.messenger.services;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.Base64UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.webauthn.api.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.flux.flux.messenger.PasskeyCredential;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.components.PasskeyChallengeCache;
import ru.flux.flux.messenger.repositories.PasskeyCredentialRepository;
import ru.flux.flux.messenger.repositories.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PasskeyService {

    private final WebAuthnManager webAuthnManager;
    private final ObjectConverter objectConverter;
    private final PublicKeyCredentialRpEntity rpEntity;
    private final PasskeyChallengeCache challengeCache;
    private final PasskeyCredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${webauthn.allowed-origins}")
    private String allowedOriginsRaw;

    public record PasskeyOptions(PublicKeyCredentialCreationOptions options, String nonce) {}
    public record AuthOptions(PublicKeyCredentialRequestOptions options, String nonce) {}

    public PasskeyOptions startPasskey(String phone) {
        PublicKeyCredentialCreationOptions options = PublicKeyCredentialCreationOptions.builder()
                .rp(rpEntity)
                .user(ImmutablePublicKeyCredentialUserEntity.builder()
                        .id(Bytes.random())
                        .name(phone)
                        .displayName(phone)
                        .build())
                .challenge(Bytes.random())
                .pubKeyCredParams(PublicKeyCredentialParameters.ES256, PublicKeyCredentialParameters.RS256)
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .userVerification(UserVerificationRequirement.PREFERRED)
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .build())
                .timeout(Duration.ofMinutes(5))
                .build();

        String nonce = challengeCache.saveRegistrationOptions(options);
        return new PasskeyOptions(options, nonce);
    }

    @Transactional
    public User completePasskey(String nonce, PublicKeyCredential<AuthenticatorAttestationResponse> credential) {
        PublicKeyCredentialCreationOptions options = challengeCache.consumeRegistrationOptions(nonce);
        if (options == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge expired or invalid nonce");
        }

        AuthenticatorAttestationResponse response = credential.getResponse();

        Set<Origin> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .map(Origin::new)
                .collect(Collectors.toSet());
        Challenge challenge = new DefaultChallenge(options.getChallenge().getBytes());
        ServerProperty serverProperty = new ServerProperty(origins, rpEntity.getId(), challenge);

        Set<String> transportStrings = response.getTransports() == null ? null
                : response.getTransports().stream()
                        .map(AuthenticatorTransport::getValue)
                        .collect(Collectors.toSet());

        RegistrationRequest registrationRequest = new RegistrationRequest(
                response.getAttestationObject().getBytes(),
                response.getClientDataJSON().getBytes(),
                transportStrings
        );
        RegistrationParameters registrationParameters = new RegistrationParameters(
                serverProperty,
                convertParams(options.getPubKeyCredParams()),
                false,
                true
        );

        RegistrationData data;
        try {
            data = webAuthnManager.verify(registrationRequest, registrationParameters);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey verification failed");
        }

        AttestationObject attObj = data.getAttestationObject();
        AuthenticatorData<RegistrationExtensionAuthenticatorOutput> authData = attObj.getAuthenticatorData();
        AttestedCredentialData credData = authData.getAttestedCredentialData();
        byte[] rawCoseKey = objectConverter.getCborConverter().writeValueAsBytes(credData.getCOSEKey());

        String credentialId = credential.getRawId().toBase64UrlString();
        String phone = options.getUser().getName();

        // Case 1: credential already registered → returning user
        Optional<PasskeyCredential> existingCred = credentialRepository.findById(credentialId);
        if (existingCred.isPresent()) {
            return existingCred.get().getUser();
        }

        // Case 2: phone known, new passkey   Case 3: fully new user
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            String base = "user_" + phone.replaceAll("[^0-9]", "");
            User newUser = User.builder()
                    .phone(phone)
                    .username(ensureUniqueUsername(base))
                    .firstName(phone)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .notifications(true)
                    .build();
            return userRepository.save(newUser);
        });

        PasskeyCredential passkey = PasskeyCredential.builder()
                .credentialId(credentialId)
                .publicKey(rawCoseKey)
                .signCount(authData.getSignCount())
                .backupEligible(authData.isFlagBE())
                .backupState(authData.isFlagBS())
                .transports(transportStrings != null ? transportStrings : new HashSet<>())
                .user(user)
                .createdAt(Instant.now())
                .build();
        credentialRepository.save(passkey);

        return user;
    }

    private String ensureUniqueUsername(String base) {
        String candidate = base;
        int i = 0;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + "_" + (++i);
        }
        return candidate;
    }

    public AuthOptions startAuthentication() {
        PublicKeyCredentialRequestOptions options = PublicKeyCredentialRequestOptions.builder()
                .challenge(Bytes.random())
                .rpId(rpEntity.getId())
                .userVerification(UserVerificationRequirement.PREFERRED)
                .timeout(Duration.ofMinutes(5))
                .build();
        String nonce = challengeCache.saveAuthenticationOptions(options);
        return new AuthOptions(options, nonce);
    }

    @Transactional
    public User completeAuthentication(String nonce,
                                       PublicKeyCredential<AuthenticatorAssertionResponse> credential) {
        PublicKeyCredentialRequestOptions options = challengeCache.consumeAuthenticationOptions(nonce);
        if (options == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge expired or invalid nonce");
        }

        String credentialId = credential.getRawId().toBase64UrlString();
        PasskeyCredential stored = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Passkey not registered for this device"));

        AuthenticatorAssertionResponse response = credential.getResponse();

        Set<Origin> origins = Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .map(Origin::new)
                .collect(Collectors.toSet());
        Challenge challenge = new DefaultChallenge(options.getChallenge().getBytes());
        ServerProperty serverProperty = new ServerProperty(origins, rpEntity.getId(), challenge);

        COSEKey coseKey = objectConverter.getCborConverter()
                .readValue(stored.getPublicKey(), COSEKey.class);
        AttestedCredentialData credData = new AttestedCredentialData(
                AAGUID.ZERO, Base64UrlUtil.decode(credentialId), coseKey);
        AuthenticatorImpl authenticator = new AuthenticatorImpl(credData, null, stored.getSignCount());

        AuthenticationRequest authRequest = new AuthenticationRequest(
                Base64UrlUtil.decode(credentialId),
                response.getAuthenticatorData().getBytes(),
                response.getClientDataJSON().getBytes(),
                response.getSignature().getBytes()
        );
        AuthenticationParameters authParams = new AuthenticationParameters(
                serverProperty, authenticator, null, false);

        AuthenticationData data;
        try {
            data = webAuthnManager.verify(authRequest, authParams);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Passkey verification failed");
        }

        stored.setSignCount(data.getAuthenticatorData().getSignCount());
        credentialRepository.save(stored);

        return stored.getUser();
    }

    private List<com.webauthn4j.data.PublicKeyCredentialParameters> convertParams(
            List<PublicKeyCredentialParameters> params) {
        return params.stream().map(p -> {
            long algValue = p.getAlg().getValue();
            var alg = com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(algValue);
            return new com.webauthn4j.data.PublicKeyCredentialParameters(
                    com.webauthn4j.data.PublicKeyCredentialType.PUBLIC_KEY, alg);
        }).toList();
    }
}