package ru.flux.android.core.network;

public class UpdateUserRequest {
    public String firstName;
    public String lastName;
    public String nickname;
    public String phone;
    public String email;
    public Boolean notifications;

    public UpdateUserRequest(String firstName, String lastName, String nickname,
                             String phone, String email, Boolean notifications) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.notifications = notifications;
    }
}