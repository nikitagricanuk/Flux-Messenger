package ru.flux.android.data;

public class PasskeyAssertionOptionsResponse {

    private String challenge;
    private String rpId;
    private long timeout;
    private String userVerification;

    public String getChallenge() {
        return challenge;
    }

    public String getRpId() {
        return rpId;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getUserVerification() {
        return userVerification;
    }
}
