package ru.flux.android.core.network;

public class PasskeyRegistrationRequest {

    private final String phone;
    private final String firstName;
    private final String lastName;
    private final String username;

    public PasskeyRegistrationRequest(String phone, String firstName, String lastName, String username) {
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }

    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
}