package ru.flux.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCESS  = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_EXPIRES = "expires_at";
    private static final long   BUFFER_MS   = 30_000L;

    private final SharedPreferences prefs;

    private static final String KEY_USER_ID = "user_id";

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    @Nullable
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public TokenManager(Context context) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    // Backend JWT expiry = 900_000 ms (15 min) per application.yaml
    private static final long ACCESS_TOKEN_TTL_MS = 900_000L;

    public void saveTokens(AuthTokens tokens) {
        saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
    }

    public void saveTokens(String accessToken, String refreshToken) {
        long expiresAt = System.currentTimeMillis() + ACCESS_TOKEN_TTL_MS;
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .putLong(KEY_EXPIRES,   expiresAt)
                .apply();
    }

    @Nullable
    public String getAccessToken()  { return prefs.getString(KEY_ACCESS,  null); }

    @Nullable
    public String getRefreshToken() { return prefs.getString(KEY_REFRESH, null); }

    public boolean isAccessTokenExpired() {
        long expiresAt = prefs.getLong(KEY_EXPIRES, 0L);
        return System.currentTimeMillis() >= expiresAt - BUFFER_MS;
    }

    public void clearTokens() { prefs.edit().clear().apply(); }
}
