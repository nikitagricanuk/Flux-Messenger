package ru.flux.android.core.data;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Contact {
    UUID id;
    String name;
    String profilePicture;

    String phoneNumber;
    String email;

    public Contact(UUID id, String name, String profilePicture, String phoneNumber, String email) {
        this.id = id;
        this.name = name;
        this.profilePicture = profilePicture;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getProfilePicture() { return profilePicture; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
}
