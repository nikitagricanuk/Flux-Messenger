package ru.flux.android.features.contacts;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ChatResponse;
import ru.flux.android.core.network.ContactResponse;
import ru.flux.android.core.network.CreateChatRequest;
import ru.flux.android.core.network.UserResponse;

public class ContactsViewModel extends AndroidViewModel {

    private static final String TAG = "ContactsViewModel";
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<ChatNavigationEvent> navigateToChat = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingContacts = new MutableLiveData<>(false);

    public ContactsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Contact>> getContacts() { return contacts; }
    public LiveData<ChatNavigationEvent> getNavigateToChat() { return navigateToChat; }
    public LiveData<Boolean> isLoadingContacts() { return loadingContacts; }

    public void loadContacts() {
        loadingContacts.setValue(true);
        try {
            ApiClient.api(getApplication()).getMyContacts().enqueue(new Callback<List<ContactResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ContactResponse>> call,
                                       @NonNull Response<List<ContactResponse>> response) {
                    loadingContacts.postValue(false);
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getMyContacts failed: " + response.code());
                        return;
                    }
                    List<Contact> list = new ArrayList<>();
                    for (ContactResponse cr : response.body()) {
                        list.add(new Contact(UUID.fromString(cr.id), cr.name,
                                cr.avatarUrl, cr.contact, null));
                    }
                    contacts.postValue(list);
                }

                @Override
                public void onFailure(@NonNull Call<List<ContactResponse>> call, @NonNull Throwable t) {
                    loadingContacts.postValue(false);
                    Log.e(TAG, "getMyContacts error", t);
                }
            });
        } catch (Exception e) {
            loadingContacts.postValue(false);
            Log.e(TAG, "ApiClient error", e);
        }
    }

    public void clearNavigateToChat() {
        navigateToChat.setValue(null);
    }

    public void openOrCreateChat(Contact contact) {
        try {
            ApiClient.api(getApplication()).getMe().enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call,
                                       @NonNull Response<UserResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getMe failed: " + response.code());
                        return;
                    }
                    String myId = response.body().id.toString();
                    createChat(contact, myId);
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "getMe error", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    private void createChat(Contact contact, String myId) {
        try {
            CreateChatRequest request = new CreateChatRequest(
                    "DIRECT", new String[]{ myId, contact.getId().toString() });

            ApiClient.api(getApplication()).createDirectChat(request)
                    .enqueue(new Callback<ChatResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<ChatResponse> call,
                                               @NonNull Response<ChatResponse> response) {
                            if (!response.isSuccessful() || response.body() == null) {
                                Log.e(TAG, "createDirectChat failed: " + response.code());
                                return;
                            }
                            ChatResponse chat = response.body();
                            String peerId = null;
                            if (chat.memberIds != null) {
                                for (String memberId : chat.memberIds) {
                                    if (!memberId.equals(myId)) {
                                        peerId = memberId;
                                        break;
                                    }
                                }
                            }
                            navigateToChat.postValue(new ChatNavigationEvent(
                                    chat.id, contact.getName(), peerId, chat.profilePicture));
                        }

                        @Override
                        public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                            Log.e(TAG, "createDirectChat error", t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    public static class ChatNavigationEvent {
        public final String chatId;
        public final String chatName;
        public final String peerId;
        public final String chatAvatarUrl;

        public ChatNavigationEvent(String chatId, String chatName,
                                   String peerId, String chatAvatarUrl) {
            this.chatId = chatId;
            this.chatName = chatName;
            this.peerId = peerId;
            this.chatAvatarUrl = chatAvatarUrl;
        }
    }
}