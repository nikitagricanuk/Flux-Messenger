package ru.flux.android.auth;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.GetPublicKeyCredentialOption;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import ru.flux.android.R;
import ru.flux.android.data.LoginRepository;
import ru.flux.android.data.PasskeyAssertionOptionsResponse;
import ru.flux.android.data.Result;

public class PasskeyAuthManager {

    private static final String TAG = "PasskeyAuthManager";

    public interface Callback {
        void onSuccess();
        void onError(@NonNull String message);
    }

    private final LoginRepository loginRepository;
    private final CredentialManager credentialManager;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PasskeyAuthManager(@NonNull ComponentActivity activity,
                              @NonNull LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        this.credentialManager = CredentialManager.create(activity);
    }

    public void signIn(@NonNull ComponentActivity activity, @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<PasskeyAssertionOptionsResponse> optionsResult =
                    loginRepository.getPasskeyAssertionOptions();
            if (optionsResult instanceof Result.Error) {
                Exception error = ((Result.Error) optionsResult).getError();
                String message = error.getMessage() != null ? error.getMessage()
                        : "Passkey options request failed";
                Log.e(TAG, message, error);
                notifyError(callback, message);
                return;
            }
            if (!(optionsResult instanceof Result.Success)) {
                notifyError(callback, "Passkey options request failed");
                return;
            }

            PasskeyAssertionOptionsResponse response =
                    ((Result.Success<PasskeyAssertionOptionsResponse>) optionsResult).getData();
            String requestJson;
            try {
                requestJson = buildCredentialRequestJson(response);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to build passkey request JSON", e);
                notifyError(callback, "Passkey options are invalid");
                return;
            }
            requestCredential(activity, requestJson, callback);
        });
    }

    private void requestCredential(@NonNull ComponentActivity activity, @NonNull String requestJson,
                                   @NonNull Callback callback) {
        mainHandler.post(() -> {
            GetPublicKeyCredentialOption passkeyOption =
                    new GetPublicKeyCredentialOption(requestJson);
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(passkeyOption)
                    .build();

            credentialManager.getCredentialAsync(
                    activity,
                    request,
                    null,
                    activity.getMainExecutor(),
                    new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Credential credential = result.getCredential();
                            if (!(credential instanceof PublicKeyCredential)) {
                                notifyError(callback, "Unsupported credential type");
                                return;
                            }
                            String credentialJson =
                                    ((PublicKeyCredential) credential).getAuthenticationResponseJson();
                            verifyCredential(credentialJson, callback);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            String message = e.getMessage() != null ? e.getMessage()
                                    : activity.getString(R.string.passkey_cancelled);
                            Log.e(TAG, "CredentialManager getCredential failed", e);
                            notifyError(callback, message);
                        }
                    }
            );
        });
    }

    private void verifyCredential(@NonNull String credentialJson, @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<String> signInResult = loginRepository.signInWithPasskey(credentialJson);
            if (signInResult instanceof Result.Success) {
                mainHandler.post(callback::onSuccess);
            } else if (signInResult instanceof Result.Error) {
                Exception error = ((Result.Error) signInResult).getError();
                String message = error.getMessage() != null ? error.getMessage()
                        : "Passkey verification failed";
                Log.e(TAG, message, error);
                notifyError(callback, message);
            } else {
                notifyError(callback, "Passkey verification failed");
            }
        });
    }

    @NonNull
    private String buildCredentialRequestJson(@NonNull PasskeyAssertionOptionsResponse response)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put("challenge", response.getChallenge());
        if (response.getRpId() != null && !response.getRpId().isEmpty()) {
            json.put("rpId", response.getRpId());
        }
        if (response.getTimeout() > 0) {
            json.put("timeout", response.getTimeout());
        }
        if (response.getUserVerification() != null && !response.getUserVerification().isEmpty()) {
            json.put("userVerification", response.getUserVerification());
        }
        return json.toString();
    }

    private void notifyError(@NonNull Callback callback, @NonNull String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
