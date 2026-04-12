package ru.flux.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsFragment extends Fragment {

    private TextView allTab, dmsTab, groupsTab;
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

        allTab    = view.findViewById(R.id.all_tab);
        dmsTab    = view.findViewById(R.id.dms_tab);
        groupsTab = view.findViewById(R.id.groups_tab);

        RecyclerView chatsRecycler = view.findViewById(R.id.chatsRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        chatsRecycler.setAdapter(adapter);

        loadChats();

        allTab.setOnClickListener(v -> setActiveTab("all"));
        dmsTab.setOnClickListener(v -> setActiveTab("dm"));
        groupsTab.setOnClickListener(v -> setActiveTab("group"));

        setActiveTab("all");
    }

    private static final String TAG = "ChatsFragment";

    private void loadChats() {
        Log.d(TAG, "loadChats: making request to /chats");
        RetrofitClient.getInstance().getApiService().getChats().enqueue(new Callback<List<ChatResponse>>() {
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

    private void setActiveTab(String filter) {
        allTab.setBackgroundResource(0);
        dmsTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);

        switch (filter) {
            case "all":   allTab.setBackgroundResource(R.drawable.bg_segment_selected);   break;
            case "dm":    dmsTab.setBackgroundResource(R.drawable.bg_segment_selected);    break;
            case "group": groupsTab.setBackgroundResource(R.drawable.bg_segment_selected); break;
        }

        adapter.setFilter(filter);
    }
}
