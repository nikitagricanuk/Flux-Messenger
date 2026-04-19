package ru.flux.android;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SignUpCompletionInternalFragment extends Fragment {

    public SignUpCompletionInternalFragment() {}

    public static SignUpCompletionInternalFragment newInstance() {
        return new SignUpCompletionInternalFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up_completion_internal, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvAtSign = view.findViewById(R.id.tvAtSign);
        EditText etUsername = view.findViewById(R.id.etUsername);

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAtSign.setTextColor(s.length() > 0 ? Color.BLACK : Color.parseColor("#FF9E9E9E"));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
