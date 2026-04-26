package ru.flux.android.core.network;

public class OAuthCodeExchangeRequest {

    private final String provider;
    private final String code;
    private final String redirectUri;
    private final String state;

    public OAuthCodeExchangeRequest(String provider, String code, String redirectUri, String state) {
        this.provider = provider;
        this.code = code;
        this.redirectUri = redirectUri;
        this.state = state;
    }
}
