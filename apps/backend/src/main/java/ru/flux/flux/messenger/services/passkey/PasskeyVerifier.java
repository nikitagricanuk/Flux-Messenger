package ru.flux.flux.messenger.services.passkey;

public interface PasskeyVerifier {

    RegistrationResult verifyRegistration(byte[] expectedChallenge,
                                          byte[] clientDataJSON,
                                          byte[] attestationObject);

    AuthenticationResult verifyAuthentication(byte[] expectedChallenge,
                                              byte[] clientDataJSON,
                                              byte[] authenticatorData,
                                              byte[] signature,
                                              byte[] storedPublicKeyCose,
                                              long storedSignCount);

    record RegistrationResult(
            byte[] credentialId,
            byte[] publicKeyCose,
            long signCount,
            String aaguid
    ) {}

    record AuthenticationResult(
            long newSignCount
    ) {}
}
