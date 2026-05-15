package ru.flux.android.features.chats;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.network.AddContactRequest;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ContactResponse;

public class ContactsViewModel extends AndroidViewModel {
    private static final String TAG = "ContactsViewModel";
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ContactsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Contact>> getContacts() { return contacts; }
    public LiveData<String> getError() { return error; }

    public void loadContacts() {
        executor.execute(() -> {
            try {
                Response<List<ContactResponse>> response =
                        ApiClient.api(getApplication()).getMyContacts().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Contact> mapped = new ArrayList<>();
                    for (ContactResponse cr : response.body()) {
                        mapped.add(new Contact(UUID.fromString(cr.id), cr.name, cr.avatarUrl, cr.contact, null));
                    }
                    contacts.postValue(mapped);
                } else {
                    Log.e(TAG, "loadContacts: failed, code=" + response.code() + " body=" + (response.errorBody() != null ? response.errorBody().string() : ""));
                    error.postValue("Не удалось загрузить контакты");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "loadContacts: exception", e);
                error.postValue(e.getMessage());
            }
        });
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
                    loadContacts();
                } else {
                    Log.e(TAG, "addContact: failed, code=" + response.code() + " body=" + (response.errorBody() != null ? response.errorBody().string() : ""));
                    error.postValue("Не удалось добавить контакт");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "addContact: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }
}