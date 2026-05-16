package ru.flux.flux.messenger;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "passkey_credentials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyCredential {

    @Id
    @Column(nullable = false, unique = true)
    private String credentialId; // Base64url-encoded bytes, used as the lookup key

    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] publicKey;    // COSE-encoded public key

    @Column(nullable = false)
    private long signCount;      // replay protection

    @Column(nullable = false)
    private boolean backupEligible;

    @Column(nullable = false)
    private boolean backupState;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "passkey_transports", joinColumns = @JoinColumn(name = "credential_id"))
    @Column(name = "transport")
    private Set<String> transports = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
