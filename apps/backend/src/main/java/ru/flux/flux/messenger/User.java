package ru.flux.flux.messenger;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column
    private String lastName;

    @Column
    private LocalDate dateOfBirth;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false, unique = true, length = 32)
    @Size(min = 3, max = 32)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;

    @Column(nullable = false, unique = true, length = 20)
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phone;

    @Column(unique = true, length = 254)
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private String email;

    @Column
    private String avatarUrl;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean notifications;

    @Builder.Default
    @ElementCollection
    private List<UUID> contacts = new ArrayList<>();

    @Column(nullable = false)
    private String password;

    public String getHandle() {
        return username;
    }

    @Override
    public String getUsername() {
        return phone;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}