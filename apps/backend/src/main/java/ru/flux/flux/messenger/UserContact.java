package ru.flux.flux.messenger;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "contact_id", nullable = false)
    private User contact;

    @Column
    private String firstNameOverride;

    @Column
    private String lastNameOverride;
}
