package ru.flux.android.features.login;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.CreatePublicKeyCredentialResponse;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.flux.android.R;
import ru.flux.android.core.Result;
import ru.flux.android.core.network.PasskeyAssertionOptions;
import ru.flux.android.core.network.PasskeyRegistrationOptions;

public class PasskeyAuthManager {

    private static final String TAG = "PasskeyAuthManager";

    public interface Callback {
        void onSuccess();
        void onError(@NonNull String message);
        void onRegistrationRequired();
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

    /** Entry point — tries sign-in first; calls onRegistrationRequired() for new users. */
    public void authenticate(@NonNull ComponentActivity activity, @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<PasskeyAssertionOptions> optionsResult =
                    loginRepository.startPasskeyAuthentication();
            if (!(optionsResult instanceof Result.Success)) {
                String msg = optionsResult instanceof Result.Error
                        ? ((Result.Error) optionsResult).getError().getMessage()
                        : "Failed to start passkey authentication";
                Log.e(TAG, "authenticate: " + msg);
                notifyError(callback, msg != null ? msg : "Failed to start passkey authentication");
                return;
            }

            PasskeyAssertionOptions options =
                    ((Result.Success<PasskeyAssertionOptions>) optionsResult).getData();
            requestGetCredential(activity, options, callback);
        });
    }

    private void requestGetCredential(@NonNull ComponentActivity activity,
                                      @NonNull PasskeyAssertionOptions options,
                                      @NonNull Callback callback) {
        mainHandler.post(() -> {
            GetPublicKeyCredentialOption passkeyOption =
                    new GetPublicKeyCredentialOption(options.getOptionsJson());
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(passkeyOption)
                    .setPreferImmediatelyAvailableCredentials(true)
                    .build();

            credentialManager.getCredentialAsync(
                    activity,
                    request,
                    null,
                    activity.getMainExecutor(),
                    new androidx.credentials.CredentialManagerCallback<
                            GetCredentialResponse, GetCredentialException>() {

                        @Override
                        public void onResult(GetCredentialResponse result) {
                            Credential credential = result.getCredential();
                            if (!(credential instanceof PublicKeyCredential)) {
                                notifyError(callback, "Unsupported credential type");
                                return;
                            }
                            String assertionJson =
                                    ((PublicKeyCredential) credential).getAuthenticationResponseJson();
                            finishAuthentication(options.getNonce(), assertionJson, callback);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            if (e instanceof NoCredentialException) {
                                Log.d(TAG, "No passkey found — routing to registration");
                                mainHandler.post(callback::onRegistrationRequired);
                            } else {
                                String msg = e.getMessage() != null ? e.getMessage()
                                        : activity.getString(R.string.passkey_cancelled);
                                Log.e(TAG, "getCredential failed", e);
                                notifyError(callback, msg);
                            }
                        }
                    }
            );
        });
    }

    private void finishAuthentication(@NonNull String nonce,
                                      @NonNull String assertionJson,
                                      @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<String> result = loginRepository.finishPasskeyAuthentication(nonce, assertionJson);
            if (result instanceof Result.Success) {
                mainHandler.post(callback::onSuccess);
            } else {
                String msg = result instanceof Result.Error
                        ? ((Result.Error) result).getError().getMessage()
                        : "Passkey authentication failed";
                Log.e(TAG, "finishAuthentication: " + msg);
                notifyError(callback, msg != null ? msg : "Passkey authentication failed");
            }
        });
    }

    /** Called after the user fills in registration details for first-time registration. */
    public void register(@NonNull ComponentActivity activity,
                         @NonNull String phone,
                         @NonNull String firstName,
                         @NonNull String lastName,
                         @NonNull String username,
                         @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<PasskeyRegistrationOptions> optionsResult =
                    loginRepository.getPasskeyRegistrationOptions(phone, firstName, lastName, username);
            if (!(optionsResult instanceof Result.Success)) {
                String msg = optionsResult instanceof Result.Error
                        ? ((Result.Error) optionsResult).getError().getMessage()
                        : "Failed to get registration options";
                Log.e(TAG, "register: " + msg);
                notifyError(callback, msg != null ? msg : "Failed to get registration options");
                return;
            }

            PasskeyRegistrationOptions options =
                    ((Result.Success<PasskeyRegistrationOptions>) optionsResult).getData();
            requestCreateCredential(activity, options, callback);
        });
    }

    private void requestCreateCredential(@NonNull ComponentActivity activity,
                                         @NonNull PasskeyRegistrationOptions options,
                                         @NonNull Callback callback) {
        mainHandler.post(() -> {
            CreatePublicKeyCredentialRequest request =
                    new CreatePublicKeyCredentialRequest(options.getOptionsJson());

            credentialManager.createCredentialAsync(
                    activity,
                    request,
                    null,
                    activity.getMainExecutor(),
                    new androidx.credentials.CredentialManagerCallback<
                            CreateCredentialResponse, CreateCredentialException>() {

                        @Override
                        public void onResult(CreateCredentialResponse result) {
                            if (!(result instanceof CreatePublicKeyCredentialResponse)) {
                                notifyError(callback, "Unsupported credential type");
                                return;
                            }
                            String registrationJson =
                                    ((CreatePublicKeyCredentialResponse) result)
                                            .getRegistrationResponseJson();
                            finishRegistration(options.getNonce(), registrationJson, callback);
                        }

                        @Override
                        public void onError(CreateCredentialException e) {
                            String msg = e.getMessage() != null ? e.getMessage()
                                    : activity.getString(R.string.passkey_cancelled);
                            Log.e(TAG, "createCredential failed", e);
                            notifyError(callback, msg);
                        }
                    }
            );
        });
    }

    private void finishRegistration(@NonNull String nonce,
                                    @NonNull String registrationJson,
                                    @NonNull Callback callback) {
        ioExecutor.execute(() -> {
            Result<String> result =
                    loginRepository.completePasskeyRegistration(nonce, registrationJson);
            if (result instanceof Result.Success) {
                mainHandler.post(callback::onSuccess);
            } else {
                String msg = result instanceof Result.Error
                        ? ((Result.Error) result).getError().getMessage()
                        : "Passkey registration failed";
                Log.e(TAG, "finishRegistration: " + msg);
                notifyError(callback, msg != null ? msg : "Passkey registration failed");
            }
        });
    }

    private void notifyError(@NonNull Callback callback, @NonNull String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}