package ru.flux.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ru.flux.android.ui.SegmentTabsView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.data.TokenManager;

public class ChatsFragment extends Fragment {

    private SegmentTabsView segmentTabs;
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView editIcon = view.findViewById(R.id.imageView);
        editIcon.setClickable(true);
        editIcon.setFocusable(true);
        editIcon.setOnClickListener(v -> {
            NewMessageBottomSheet sheet = new NewMessageBottomSheet();
            sheet.show(getParentFragmentManager(), NewMessageBottomSheet.class.getSimpleName());
        });

        segmentTabs = view.findViewById(R.id.segment_tabs);

        RecyclerView chatsRecycler = view.findViewById(R.id.chatsRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        chatsRecycler.setAdapter(adapter);

        loadChats();

        String[] filters = {"all", "dm", "group"};
        segmentTabs.setOnTabSelectedListener(index -> {
            if (index < filters.length) adapter.setFilter(filters[index]);
        });
        adapter.setFilter("all");
    }

    private static final String TAG = "ChatsFragment";

    private void loadChats() {
        Log.d(TAG, "loadChats: making request to /chats");
        try {
            TokenManager tokenManager = new TokenManager(requireContext());
            ApiService apiService = ApiClient.getInstance(tokenManager).create(ApiService.class);
            apiService.getChats().enqueue(new Callback<List<ChatResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<ChatResponse>> call,
                                       @NonNull Response<List<ChatResponse>> response) {
                    Log.d(TAG, "onResponse: code=" + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<Chat> chats = new ArrayList<>();
                        for (ChatResponse cr : response.body()) {
                            chats.add(toChat(cr));
                        }
                        adapter.setChats(chats);
                    } else {
                        Log.e(TAG, "onResponse: unsuccessful, code=" + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<ChatResponse>> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getClass().getName() + ": " + t.getMessage());
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "loadChats: TokenManager init failed: " + e.getMessage());
        }
    }

    private Chat toChat(ChatResponse cr) {
        String type = "DIRECT".equals(cr.type) ? "dm" : "group";
        String time = "";
        // lastMessageAt is ISO-8601: "2026-04-12T14:19:23" — grab HH:mm after the T
        if (cr.lastMessageAt != null) {
            int tIndex = cr.lastMessageAt.indexOf('T');
            if (tIndex >= 0 && cr.lastMessageAt.length() >= tIndex + 6) {
                time = cr.lastMessageAt.substring(tIndex + 1, tIndex + 6); // "HH:mm"
            }
        }
        return new Chat(cr.id, cr.name, cr.lastMessage, cr.profilePicture, time, type);
    }

}
