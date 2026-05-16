package ru.flux.android.features.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.MainActivity;
import ru.flux.android.R;
import ru.flux.android.core.auth.TokenManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (hasActiveSession()) {
            openMainScreen();
        }
    }

    private boolean hasActiveSession() {
        try {
            TokenManager tokenManager = new TokenManager(this);
            return tokenManager.getRefreshToken() != null;
        } catch (GeneralSecurityException | IOException ignored) {
            return false;
        }
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
