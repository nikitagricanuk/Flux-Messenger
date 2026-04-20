package ru.flux.flux.messenger;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_passkey")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasskey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 1024)
    private byte[] credentialId;

    @Column(nullable = false, length = 2048)
    private byte[] publicKeyCose;

    @Column(nullable = false)
    private long signCount;

    @Column
    private UUID aaguid;

    @Column(length = 128)
    private String transports;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant lastUsedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
