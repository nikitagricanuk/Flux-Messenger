package ru.flux.android.features.chats;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import ru.flux.android.core.network.AddContactRequest;
import ru.flux.android.core.network.ApiClient;

public class ContactsViewModel extends AndroidViewModel {
    private static final String TAG = "ContactsViewModel";
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ContactsViewModel(@NonNull Application application) {
        super(application);
    }

    public void addContact(String phone, String firstName, String lastName) {
        Log.d(TAG, "addContact: phone=" + phone + ", name=" + firstName + " " + lastName);
        executor.execute(() -> {
            try {
                Response<Void> response = ApiClient.api(getApplication()).addContact(
                        new AddContactRequest(phone, firstName, lastName)
                ).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "addContact: success");
                } else {
                    Log.e(TAG, "addContact: failed, code=" + response.code());
                    error.postValue("Не удалось добавить контакт");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "addContact: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }
}
