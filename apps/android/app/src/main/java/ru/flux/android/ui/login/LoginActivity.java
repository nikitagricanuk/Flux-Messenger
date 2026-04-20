package ru.flux.android.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.MainActivity;
import ru.flux.android.R;
import ru.flux.android.data.TokenManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            TokenManager tokenManager = new TokenManager(this);
            if (!tokenManager.isAccessTokenExpired()) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
        } catch (GeneralSecurityException | IOException ignored) {}

        setContentView(R.layout.activity_login);
    }
}