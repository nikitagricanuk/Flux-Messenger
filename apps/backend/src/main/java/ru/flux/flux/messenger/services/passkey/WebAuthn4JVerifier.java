package ru.flux.flux.messenger.services.passkey;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.credential.CredentialRecordImpl;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.server.ServerProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.flux.flux.messenger.exceptions.PasskeyVerificationException;

import java.util.List;
import java.util.UUID;

@Component
public class WebAuthn4JVerifier implements PasskeyVerifier {

    private final WebAuthnManager manager;
    private final ObjectConverter objectConverter;
    private final Origin origin;
    private final String rpId;

    public WebAuthn4JVerifier(@Value("${webauthn.rp-id:localhost}") String rpId,
                              @Value("${webauthn.origin:http://localhost:8080}") String origin) {
        this.manager = WebAuthnManager.createNonStrictWebAuthnManager();
        this.objectConverter = new ObjectConverter();
        this.origin = new Origin(origin);
        this.rpId = rpId;
    }

    @Override
    public RegistrationResult verifyRegistration(byte[] expectedChallenge,
                                                 byte[] clientDataJSON,
                                                 byte[] attestationObject) {
        try {
            ServerProperty serverProperty = new ServerProperty(origin, rpId, new DefaultChallenge(expectedChallenge));
            RegistrationRequest request = new RegistrationRequest(attestationObject, clientDataJSON);
            List<PublicKeyCredentialParameters> pubKeyCredParams = List.of(
                    new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256),
                    new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)
            );
            RegistrationParameters params = new RegistrationParameters(serverProperty, pubKeyCredParams, false, true);
            RegistrationData data = manager.verify(request, params);

            AttestationObject ao = data.getAttestationObject();
            if (ao == null) {
                throw new PasskeyVerificationException("Missing attestation object");
            }
            AttestedCredentialData acd = ao.getAuthenticatorData().getAttestedCredentialData();
            if (acd == null) {
                throw new PasskeyVerificationException("Missing attested credential data");
            }
            byte[] credentialId = acd.getCredentialId();
            byte[] cosePublicKey = objectConverter.getCborConverter().writeValueAsBytes(acd.getCOSEKey());
            long signCount = ao.getAuthenticatorData().getSignCount();
            AAGUID aaguid = acd.getAaguid();
            return new RegistrationResult(credentialId, cosePublicKey, signCount, aaguid == null ? null : aaguid.toString());
        } catch (PasskeyVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new PasskeyVerificationException("Passkey registration verification failed: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthenticationResult verifyAuthentication(byte[] expectedChallenge,
                                                     byte[] clientDataJSON,
                                                     byte[] authenticatorData,
                                                     byte[] signature,
                                                     byte[] storedPublicKeyCose,
                                                     long storedSignCount) {
        try {
            ServerProperty serverProperty = new ServerProperty(origin, rpId, new DefaultChallenge(expectedChallenge));

            var coseKey = objectConverter.getCborConverter()
                    .readValue(storedPublicKeyCose, com.webauthn4j.data.attestation.authenticator.COSEKey.class);
            AttestedCredentialData acd = new AttestedCredentialData(AAGUID.NULL, new byte[0], coseKey);
            CredentialRecord credentialRecord = new CredentialRecordImpl(
                    null, null, null, null, storedSignCount, acd, null, null, null, null
            );

            AuthenticationRequest request = new AuthenticationRequest(
                    null,
                    authenticatorData,
                    clientDataJSON,
                    signature
            );
            AuthenticationParameters params = new AuthenticationParameters(
                    serverProperty,
                    credentialRecord,
                    null,
                    false,
                    true
            );
            AuthenticationData data = manager.verify(request, params);
            long newSignCount = data.getAuthenticatorData().getSignCount();
            return new AuthenticationResult(newSignCount);
        } catch (Exception e) {
            throw new PasskeyVerificationException("Passkey authentication verification failed: " + e.getMessage(), e);
        }
    }
}
