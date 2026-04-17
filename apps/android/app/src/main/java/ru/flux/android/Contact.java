package ru.flux.android;

import java.util.UUID;

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
}
