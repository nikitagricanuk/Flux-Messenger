package ru.flux.android.data;

public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String phone;
    private String password;
    private String avatarUrl;

    public SignUpRequest(String firstName, String lastName, String username,
                         String phone, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.phone = phone;
        this.password = password;
    }
}
