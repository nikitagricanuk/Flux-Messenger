package ru.flux.flux.messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column()
    private String avatarUrl;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @ElementCollection
    private List<String> groups = new ArrayList<>();

    protected Contact() {}

    public Contact(String avatarUrl, String name, String surname, String phoneNumber, List<String> groups) {
        this.avatarUrl = avatarUrl;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact contact)) return false;
        return id != null && id.equals(contact.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
