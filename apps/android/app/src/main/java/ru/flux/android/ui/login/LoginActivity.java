package ru.flux.android.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.flux.android.MainActivity;
import ru.flux.android.R;
import ru.flux.android.auth.OAuthManager;
import ru.flux.android.data.AuthRepositoryFactory;
import ru.flux.android.data.LoginRepository;
import ru.flux.android.data.Result;
import ru.flux.android.data.TokenManager;

public class LoginActivity extends AppCompatActivity {

    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (handleOAuthCallback(getIntent())) {
            return;
        }

        if (hasActiveSession()) {
            openMainScreen();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOAuthCallback(intent);
    }

    private boolean handleOAuthCallback(Intent intent) {
        Uri callbackUri = intent != null ? intent.getData() : null;
        if (!OAuthManager.isOAuthCallback(callbackUri)) {
            return false;
        }
        setIntent(new Intent(intent).setData(null));

        OAuthManager.OAuthCallbackPayload payload =
                OAuthManager.parseCallback(this, callbackUri);

        if (payload.getError() != null) {
            Toast.makeText(this, R.string.oauth_error, Toast.LENGTH_LONG).show();
            return true;
        }

        if (payload.getAccessToken() != null && payload.getRefreshToken() != null) {
            saveTokensAndOpenMain(payload.getAccessToken(), payload.getRefreshToken());
            return true;
        }

        if (payload.getProvider() == null || payload.getCode() == null) {
            Toast.makeText(this, R.string.oauth_missing_callback_data, Toast.LENGTH_LONG).show();
            return true;
        }

        exchangeOAuthCode(payload);
        return true;
    }

    private void exchangeOAuthCode(@NonNull OAuthManager.OAuthCallbackPayload payload) {
        ioExecutor.execute(() -> {
            try {
                LoginRepository repository = AuthRepositoryFactory.create(this);
                Result<String> result = repository.exchangeOAuthCode(
                        payload.getProvider(),
                        payload.getCode(),
                        OAuthManager.getRedirectUri(),
                        payload.getState()
                );
                if (result instanceof Result.Success) {
                    runOnUiThread(this::openMainScreen);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, R.string.oauth_error,
                            Toast.LENGTH_LONG).show());
                }
            } catch (GeneralSecurityException | IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.auth_init_failed,
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveTokensAndOpenMain(String accessToken, String refreshToken) {
        ioExecutor.execute(() -> {
            try {
                LoginRepository repository = AuthRepositoryFactory.create(this);
                repository.saveTokens(accessToken, refreshToken);
                runOnUiThread(this::openMainScreen);
            } catch (GeneralSecurityException | IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.auth_init_failed,
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private boolean hasActiveSession() {
        try {
            TokenManager tokenManager = new TokenManager(this);
            return !tokenManager.isAccessTokenExpired();
        } catch (GeneralSecurityException | IOException ignored) {
            return false;
        }
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
