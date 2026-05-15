package ru.flux.android.core.network;

public class UpdateUserRequest {
    public String firstName;
    public String lastName;
    public String nickname;
    public String dateOfBirth;
    public String phone;
    public String email;
    public String avatarUrl;
    public Boolean notifications;
    public String bio;

    public UpdateUserRequest(String firstName, String lastName, String nickname,
                             String dateOfBirth, String phone, String email,
                             Boolean notifications, String bio, String avatarUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.notifications = notifications;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }
}
