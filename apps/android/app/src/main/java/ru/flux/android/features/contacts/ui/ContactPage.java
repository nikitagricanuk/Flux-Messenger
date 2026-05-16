package ru.flux.android.features.contacts.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.R;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ChatResponse;
import ru.flux.android.core.network.ContactResponse;
import ru.flux.android.core.network.CreateChatRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.features.contacts.ContactAdapter;

public class ContactPage extends Fragment {

    private static final String TAG = "ContactPage";
    private ContactAdapter adapter;
    String peerId = null;

    public ContactPage() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_page, container, false);
    }

    private void openOrCreateChat(Contact contact) {
        try {
            ApiClient.api(requireContext()).getMe().enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call,
                                       @NonNull Response<UserResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getMe failed: " + response.code());
                        return;
                    }

                    String myId = response.body().id.toString();
                    CreateChatRequest request = new CreateChatRequest(
                            "DIRECT",
                            new String[]{ myId, contact.getId().toString() }
                    );

                    try {
                        ApiClient.api(requireContext()).createDirectChat(request)
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

                                        Bundle args = new Bundle();
                                        args.putString("chatId", chat.id.toString());
                                        args.putString("chatName", contact.getName());
                                        args.putString("peerId", peerId);
                                        args.putBoolean("isGroup", false);
                                        args.putString("chatAvatarUrl", chat.profilePicture);
                                        NavHostFragment.findNavController(ContactPage.this)
                                                .navigate(R.id.action_contactPage_to_chatFragment, args);
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<ChatResponse> call,
                                                          @NonNull Throwable t) {
                                        Log.e(TAG, "createDirectChat error", t);
                                    }
                                });
                    } catch (GeneralSecurityException | IOException e) {
                        Log.e(TAG, "ApiClient error on createDirectChat", e);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "getMe error", t);
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "ApiClient error on getMe", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.contactsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ContactAdapter(new ArrayList<>(), this::openOrCreateChat);
        recyclerView.setAdapter(adapter);

        loadContacts();
    }

    private void loadContacts() {
        try {
            ApiClient.api(requireContext()).getMyContacts().enqueue(new Callback<List<ContactResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ContactResponse>> call,
                                       @NonNull Response<List<ContactResponse>> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getMyContacts failed: " + response.code());
                        return;
                    }
                    List<Contact> contacts = new ArrayList<>();
                    for (ContactResponse cr : response.body()) {
                        contacts.add(new Contact(
                                UUID.fromString(cr.id),
                                cr.name,
                                cr.avatarUrl,
                                cr.contact,
                                null
                        ));
                    }
                    adapter.setContacts(contacts);
                }

                @Override
                public void onFailure(@NonNull Call<List<ContactResponse>> call, @NonNull Throwable t) {
                    Log.e(TAG, "getMyContacts error", t);
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }
}