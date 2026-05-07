package ru.flux.android.core.network;

public class PasskeyAssertionRequest {

    private String id;
    private String rawId;
    private String type;
    private Response response;

    public String getRawId() {
        return rawId;
    }

    public Response getResponse() {
        return response;
    }

    public static class Response {
        private String clientDataJSON;
        private String authenticatorData;
        private String signature;
        private String userHandle;

        public String getClientDataJSON() {
            return clientDataJSON;
        }

        public String getAuthenticatorData() {
            return authenticatorData;
        }

        public String getSignature() {
            return signature;
        }
    }
}
