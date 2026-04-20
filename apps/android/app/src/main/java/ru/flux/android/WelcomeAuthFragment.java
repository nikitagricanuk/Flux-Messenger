package ru.flux.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.auth.OAuthManager;
import ru.flux.android.auth.PasskeyAuthManager;
import ru.flux.android.data.AuthRepositoryFactory;

public class WelcomeAuthFragment extends Fragment {

    @Nullable
    private PasskeyAuthManager passkeyAuthManager;

    public WelcomeAuthFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            passkeyAuthManager = new PasskeyAuthManager((ComponentActivity) requireActivity(),
                    AuthRepositoryFactory.create(requireContext()));
        } catch (GeneralSecurityException | IOException e) {
            passkeyAuthManager = null;
            Toast.makeText(requireContext(), R.string.auth_init_failed, Toast.LENGTH_LONG).show();
        }

        MaterialButton loginBtn = view.findViewById(R.id.login);
        MaterialButton signupBtn = view.findViewById(R.id.signup);
        MaterialButton passkeyBtn = view.findViewById(R.id.materialButton);
        MaterialButton githubBtn = view.findViewById(R.id.materialButton3);
        MaterialButton googleBtn = view.findViewById(R.id.materialButton2);

        loginBtn.setEnabled(true);
        signupBtn.setEnabled(true);

        loginBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_welcome_to_login));
        signupBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_welcome_to_signup));

        passkeyBtn.setOnClickListener(v -> startPasskeySignIn());
        githubBtn.setOnClickListener(v -> startOAuth(OAuthManager.PROVIDER_GITHUB));
        googleBtn.setOnClickListener(v -> startOAuth(OAuthManager.PROVIDER_GOOGLE));
    }

    private void startPasskeySignIn() {
        if (passkeyAuthManager == null) {
            Toast.makeText(requireContext(), R.string.auth_init_failed, Toast.LENGTH_LONG).show();
            return;
        }

        passkeyAuthManager.signIn((ComponentActivity) requireActivity(),
                new PasskeyAuthManager.Callback() {
                    @Override
                    public void onSuccess() {
                        openMainScreen();
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void startOAuth(@NonNull String provider) {
        try {
            OAuthManager.startOAuth(requireActivity(), provider);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(requireContext(), R.string.oauth_browser_not_found,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openMainScreen() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
