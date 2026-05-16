package ru.flux.android.core.network;

public class PasskeyAssertionOptions {

    private final String optionsJson;
    private final String nonce;

    public PasskeyAssertionOptions(String optionsJson, String nonce) {
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