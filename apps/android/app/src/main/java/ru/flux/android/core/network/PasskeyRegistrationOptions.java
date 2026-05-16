package ru.flux.android.core.network;

public class PasskeyRegistrationOptions {

    private final String optionsJson;
    private final String nonce;

    public PasskeyRegistrationOptions(String optionsJson, String nonce) {
        this.optionsJson = optionsJson;
        this.nonce = nonce;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public String getNonce() {
        return nonce;
    }
}