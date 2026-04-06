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

import java.util.Arrays;
import java.util.List;

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

        // Set up RecyclerView
        RecyclerView chatsRecycler = view.findViewById(R.id.chatsRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        chatsRecycler.setAdapter(adapter);

        // Load data (replace with real API call later)
        adapter.setChats(getDummyChats());

        // Tab clicks
        allTab.setOnClickListener(v -> setActiveTab("all"));
        dmsTab.setOnClickListener(v -> setActiveTab("dm"));
        groupsTab.setOnClickListener(v -> setActiveTab("group"));

        setActiveTab("all");
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

    // Dummy data so you can see the list before the API is ready.
    // Replace this whole method with a Retrofit call later.
    private List<Chat> getDummyChats() {
        return Arrays.asList(
            new Chat("1", "Евгений Сафонов",      "Пример крутого сообщения. Это сообщение имеет очень умный…", null, "18:05", "dm"),
            new Chat("2", "✨Звёздочки политеха🍕", "Пример крутого сообщения. Это сообщение имеет очень умный…", null, "18:05", "group"),
            new Chat("3", "Никита Грицанюк",      "Пример крутого сообщения. Это сообщение имеет очень умный…", null, "18:05", "dm"),
            new Chat("4", "ДримТим ИрНИТУ ± ИГУ", "Пример крутого сообщения. Это сообщение имеет очень умный…", null, "18:05", "group"),
            new Chat("5", "Mikhail Katashevtsev",  "Пример крутого сообщения. Это сообщение имеет очень умный…", null, "18:05", "dm")
        );
    }
}
