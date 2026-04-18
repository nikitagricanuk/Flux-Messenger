package ru.flux.flux.messenger.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.flux.flux.messenger.OAuthProvider;
import ru.flux.flux.messenger.UserOAuthIdentity;

import java.util.Optional;
import java.util.UUID;

public interface UserOAuthIdentityRepository extends JpaRepository<UserOAuthIdentity, UUID> {
    Optional<UserOAuthIdentity> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
