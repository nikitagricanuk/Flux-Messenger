package ru.flux.android.core.network;

public class PasskeyRegistrationRequest {

    private final String phone;

    public PasskeyRegistrationRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }
}