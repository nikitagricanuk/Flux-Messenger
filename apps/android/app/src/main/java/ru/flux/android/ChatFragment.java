package ru.flux.android;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String chatName = getArguments() != null
                ? getArguments().getString("chatName", "Чат") : "Чат";
        boolean isGroup = getArguments() != null
                && getArguments().getBoolean("isGroup", false);

        ((TextView) view.findViewById(R.id.chatName)).setText(chatName);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        view.findViewById(R.id.chatHeader).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_chatFragment_to_profileFragment)
        );

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("1", "Пример сообщения", "other", "Евгений", "", "12:00", false));
        messages.add(new Message("2", "Пример сообщения", "me", "", "", "12:01", true));
        messages.add(new Message("3", "Пример более длинного сообщения", "other", "Евгений", "", "12:02", false));
        messages.add(new Message("4", "Пример сообщения", "me", "", "", "12:03", true));

        RecyclerView recyclerView = view.findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new ChatMessangerAdapter(messages, isGroup));
    }
}