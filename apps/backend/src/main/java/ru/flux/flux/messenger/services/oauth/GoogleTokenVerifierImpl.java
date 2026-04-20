package ru.flux.flux.messenger.services.oauth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.flux.flux.messenger.exceptions.OAuthVerificationException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierImpl(@Value("${oauth.google.client-id:}") String clientId) {
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(transport, jsonFactory);
        if (clientId != null && !clientId.isBlank()) {
            builder.setAudience(Collections.singletonList(clientId));
        }
        this.verifier = builder.build();
    }

    @Override
    public OAuthUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new OAuthVerificationException("Missing Google ID token");
        }
        GoogleIdToken token;
        try {
            token = verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new OAuthVerificationException("Failed to verify Google ID token", e);
        }
        if (token == null) {
            throw new OAuthVerificationException("Invalid Google ID token");
        }
        GoogleIdToken.Payload payload = token.getPayload();
        String sub = payload.getSubject();
        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String avatar = (String) payload.get("picture");
        return new OAuthUserInfo(sub, email, firstName, lastName, avatar);
    }
}
