package ru.flux.android.auth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.Locale;
import java.util.UUID;

import ru.flux.android.BuildConfig;

public final class OAuthManager {

    public static final String PROVIDER_GOOGLE = "google";
    public static final String PROVIDER_GITHUB = "github";

    private static final String OAUTH_PREFS = "oauth_prefs";
    private static final String KEY_PENDING_STATE = "pending_state";
    private static final String KEY_PENDING_PROVIDER = "pending_provider";

    private OAuthManager() {}

    public static void startOAuth(@NonNull Activity activity, @NonNull String provider) {
        String normalizedProvider = provider.trim().toLowerCase(Locale.US);
        String state = UUID.randomUUID().toString();
        savePendingState(activity, normalizedProvider, state);

        Uri callbackUri = getCallbackUri();
        String startPath = String.format(Locale.US, BuildConfig.OAUTH_START_PATH_TEMPLATE,
                normalizedProvider);

        Uri startUri = resolveUri(startPath).buildUpon()
                .appendQueryParameter("redirect_uri", callbackUri.toString())
                .appendQueryParameter("redirectUri", callbackUri.toString())
                .appendQueryParameter("state", state)
                .build();

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).build();
        customTabsIntent.launchUrl(activity, startUri);
    }

    public static boolean isOAuthCallback(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        }
        String expectedScheme = BuildConfig.OAUTH_CALLBACK_SCHEME;
        String expectedHost = BuildConfig.OAUTH_CALLBACK_HOST;
        String expectedPath = BuildConfig.OAUTH_CALLBACK_PATH;

        if (!expectedScheme.equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        if (!expectedHost.equalsIgnoreCase(uri.getHost())) {
            return false;
        }
        String path = uri.getPath();
        return expectedPath.equals(path);
    }

    @NonNull
    public static OAuthCallbackPayload parseCallback(@NonNull Context context, @NonNull Uri uri) {
        String error = firstNonEmpty(uri.getQueryParameter("error"),
                uri.getQueryParameter("error_description"));

        String provider = firstNonEmpty(
                uri.getQueryParameter("provider"),
                getPrefs(context).getString(KEY_PENDING_PROVIDER, null)
        );

        String expectedState = getPrefs(context).getString(KEY_PENDING_STATE, null);
        String callbackState = uri.getQueryParameter("state");

        if (expectedState != null && !expectedState.equals(callbackState)) {
            clearPendingState(context);
            return OAuthCallbackPayload.error("invalid_state");
        }

        String code = uri.getQueryParameter("code");
        String accessToken = firstNonEmpty(uri.getQueryParameter("access_token"),
                uri.getQueryParameter("accessToken"));
        String refreshToken = firstNonEmpty(uri.getQueryParameter("refresh_token"),
                uri.getQueryParameter("refreshToken"));

        clearPendingState(context);

        if (error != null) {
            return OAuthCallbackPayload.error(error);
        }
        return new OAuthCallbackPayload(provider, code, callbackState, accessToken, refreshToken,
                null);
    }

    @NonNull
    public static String getRedirectUri() {
        return getCallbackUri().toString();
    }

    @NonNull
    private static Uri resolveUri(@NonNull String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return Uri.parse(path);
        }
        String base = BuildConfig.BACKEND_BASE_URL;
        boolean baseEndsWithSlash = base.endsWith("/");
        boolean pathStartsWithSlash = path.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash) {
            return Uri.parse(base + path.substring(1));
        }
        if (!baseEndsWithSlash && !pathStartsWithSlash) {
            return Uri.parse(base + "/" + path);
        }
        return Uri.parse(base + path);
    }

    @NonNull
    private static Uri getCallbackUri() {
        return new Uri.Builder()
                .scheme(BuildConfig.OAUTH_CALLBACK_SCHEME)
                .authority(BuildConfig.OAUTH_CALLBACK_HOST)
                .path(trimStartingSlash(BuildConfig.OAUTH_CALLBACK_PATH))
                .build();
    }

    private static void savePendingState(Context context, String provider, String state) {
        getPrefs(context)
                .edit()
                .putString(KEY_PENDING_PROVIDER, provider)
                .putString(KEY_PENDING_STATE, state)
                .apply();
    }

    public static void clearPendingState(Context context) {
        getPrefs(context)
                .edit()
                .remove(KEY_PENDING_PROVIDER)
                .remove(KEY_PENDING_STATE)
                .apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(OAUTH_PREFS, Context.MODE_PRIVATE);
    }

    @Nullable
    private static String firstNonEmpty(@Nullable String a, @Nullable String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        if (b != null && !b.isEmpty()) {
            return b;
        }
        return null;
    }

    @NonNull
    private static String trimStartingSlash(@NonNull String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public static final class OAuthCallbackPayload {

        @Nullable private final String provider;
        @Nullable private final String code;
        @Nullable private final String state;
        @Nullable private final String accessToken;
        @Nullable private final String refreshToken;
        @Nullable private final String error;

        OAuthCallbackPayload(@Nullable String provider, @Nullable String code, @Nullable String state,
                             @Nullable String accessToken, @Nullable String refreshToken,
                             @Nullable String error) {
            this.provider = provider;
            this.code = code;
            this.state = state;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.error = error;
        }

        static OAuthCallbackPayload error(String error) {
            return new OAuthCallbackPayload(null, null, null, null, null, error);
        }

        @Nullable
        public String getProvider() {
            return provider;
        }

        @Nullable
        public String getCode() {
            return code;
        }

        @Nullable
        public String getState() {
            return state;
        }

        @Nullable
        public String getAccessToken() {
            return accessToken;
        }

        @Nullable
        public String getRefreshToken() {
            return refreshToken;
        }

        @Nullable
        public String getError() {
            return error;
        }
    }
}
