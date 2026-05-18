package ru.flux.flux;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;

@Configuration
public class PasskeyConfig {

    @Bean
    public WebAuthnManager webAuthnManager() {
        return WebAuthnManager.createNonStrictWebAuthnManager();
    }

    @Bean
    public ObjectConverter objectConverter() {
        return new ObjectConverter();
    }

    // The WebauthnJacksonModule jar has no META-INF/services entry, so
    // Jackson SPI auto-discovery skips it. Register it as a bean so Spring
    // Boot wires the WebAuthn deserializers into the shared ObjectMapper —
    // without this, @RequestBody PublicKeyCredential<...> fails to deserialize.
    @Bean
    public WebauthnJacksonModule webauthnJacksonModule() {
        return new WebauthnJacksonModule();
    }

    @Bean
    public PublicKeyCredentialRpEntity rpEntity(
            @Value("${webauthn.rp-id}") String rpId,
            @Value("${webauthn.rp-name}") String rpName) {
        return PublicKeyCredentialRpEntity.builder()
                .id(rpId)
                .name(rpName)
                .build();
    }
}