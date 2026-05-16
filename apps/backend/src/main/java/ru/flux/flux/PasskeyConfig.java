package ru.flux.flux;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;

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