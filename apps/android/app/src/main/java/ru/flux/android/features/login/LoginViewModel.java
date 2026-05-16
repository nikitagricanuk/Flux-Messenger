package ru.flux.android.features.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.flux.android.R;
import ru.flux.android.core.Result;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> signUpResult = new MutableLiveData<>();
    private final Executor executor = Executors.newSingleThreadExecutor();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<LoginResult> getSignUpResult() {
        return signUpResult;
    }

    public void login(String phone, String password) {
        Log.d(TAG, "login: phone=" + phone);
        executor.execute(() -> {
            Result<String> result = loginRepository.login(phone, password);
            if (result instanceof Result.Success) {
                Log.d(TAG, "login: success");
                loginResult.postValue(new LoginResult(new LoggedInUserView(phone)));
            } else {
                Log.e(TAG, "login: failed — " + ((Result.Error) result).getError().getMessage());
                loginResult.postValue(new LoginResult(R.string.login_failed));
            }
        });
    }

    public void signUp(String firstName, String lastName, String username,
                       String phone, String password) {
        Log.d(TAG, "signUp: phone=" + phone + ", username=" + username);
        executor.execute(() -> {
            Result<String> result = loginRepository.signUp(firstName, lastName, username, phone, password);
            if (result instanceof Result.Success) {
                Log.d(TAG, "signUp: success");
                signUpResult.postValue(new LoginResult(new LoggedInUserView(phone)));
            } else {
                Log.e(TAG, "signUp: failed — " + ((Result.Error) result).getError().getMessage());
                signUpResult.postValue(new LoginResult(R.string.login_failed));
            }
        });
    }

    public void loginDataChanged(String phone, String password) {
        if (!isPhoneValid(phone)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder phone validation check
    private boolean isPhoneValid(String phone) {
        if (phone == null) {
            return false;
        }

//        if (phone.contains("@")) {
//            return Patterns.EMAIL_ADDRESS.matcher(phone).matches();
//        }
        else {
            return !phone.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}