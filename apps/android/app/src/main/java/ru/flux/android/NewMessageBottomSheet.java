package ru.flux.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.data.CreateChatRequest;
import ru.flux.android.data.TokenManager;

public class NewMessageBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "NewMessageBottomSheet";
    private NewMessageAdapter adapter;
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    public int getTheme() {
        return R.style.BottomSheetStyle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_new_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация
        try {
            tokenManager = new TokenManager(requireContext());
            apiService = ApiClient.getInstance(tokenManager).create(ApiService.class);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Init failed", e);
            return;
        }

        view.findViewById(R.id.cancelButton).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.newGroupButton).setOnClickListener(v -> {
            // TODO: open new group flow
        });

        view.findViewById(R.id.newContactButton).setOnClickListener(v -> {
            // TODO: open new contact flow
        });

        // RecyclerView
        RecyclerView recycler = view.findViewById(R.id.contactsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(true);

        adapter = new NewMessageAdapter(new ArrayList<>(), this::openOrCreateChat);
        recycler.setAdapter(adapter);

        //Тестовый функционал в теории его можно удалить
        EditText searchInput = view.findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchUsers(query);
                } else {
                    loadContacts();
                }
            }
        });

        loadContacts();
    }

    private void searchUsers(String query) {
        apiService.searchUsers(query).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<UserResponse>> call,
                                   @NonNull Response<List<UserResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<Contact> contacts = new ArrayList<>();
                for (UserResponse u : response.body()) {
                    String name = u.firstName + (u.lastName != null ? " " + u.lastName : "");
                    contacts.add(new Contact(
                            UUID.fromString(u.id), 
                            name,
                            u.avatarUrl,
                            u.phone,
                            u.email
                    ));
                }
                adapter.setContacts(contacts);
            }

            @Override
            public void onFailure(@NonNull Call<List<UserResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Search error", t);
            }
        });
    }

    private void loadContacts() {
        apiService.getMyContacts().enqueue(new Callback<>() {
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
                            UUID.fromString(cr.id), cr.name, cr.avatarUrl, cr.contact, null
                    ));
                }
                adapter.setContacts(contacts);
            }

            @Override
            public void onFailure(@NonNull Call<List<ContactResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "getMyContacts error", t);
            }
        });
    }

    private void openOrCreateChat(Contact contact) {
        CreateChatRequest request = new CreateChatRequest(
                "DIRECT",
                null,
                Collections.singletonList(contact.id)
        );

        apiService.createChat(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call,
                                   @NonNull Response<ChatResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "createChat failed: " + response.code());
                    return;
                }
                ChatResponse chat = response.body();

                Bundle args = new Bundle();
                args.putString("chatId", chat.id);
                args.putString("chatName", contact.name);
                args.putBoolean("isGroup", false);

                dismiss();

                NavHostFragment.findNavController(requireParentFragment())
                        .navigate(R.id.action_chatsFragment_to_chatFragment, args);
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "createChat error", t);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        int screenHeight = requireActivity().getWindow().getDecorView().getHeight();
        ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
        lp.height = (int) (screenHeight * 0.92);
        bottomSheet.setLayoutParams(lp);

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}