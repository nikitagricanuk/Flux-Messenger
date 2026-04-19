package ru.flux.android;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.material.card.MaterialCardView;

public class SignUpCompletion3rdPartyFragment extends Fragment {

    public SignUpCompletion3rdPartyFragment() {}

    public static SignUpCompletion3rdPartyFragment newInstance() {
        return new SignUpCompletion3rdPartyFragment();
    }

    private void setupPhoneFormatting(EditText etPhone) {
        etPhone.setText("+7 (");
        Selection.setSelection(etPhone.getText(), etPhone.getText().length());

        etPhone.addTextChangedListener(new TextWatcher() {
            private boolean updating = false;
            private String lastFormatted = "+7 (";

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;

                String raw = s.toString();
                String digits = raw.replaceAll("[^\\d]", "");
                if (digits.startsWith("7")) digits = digits.substring(1);
                if (digits.length() > 10) digits = digits.substring(0, 10);

                // If user deleted a separator (text shorter, digits unchanged), remove one digit too
                if (raw.length() < lastFormatted.length()) {
                    String prevDigits = lastFormatted.replaceAll("[^\\d]", "");
                    if (prevDigits.startsWith("7")) prevDigits = prevDigits.substring(1);
                    if (digits.equals(prevDigits) && !digits.isEmpty()) {
                        digits = digits.substring(0, digits.length() - 1);
                    }
                }

                String formatted = formatPhone(digits);
                lastFormatted = formatted;
                s.replace(0, s.length(), formatted);
                Selection.setSelection(s, s.length());

                updating = false;
            }

            private String formatPhone(String d) {
                int len = d.length();
                StringBuilder sb = new StringBuilder("+7");
                if (len == 0) return sb.toString();
                sb.append(" (").append(d, 0, Math.min(3, len));
                if (len < 3) return sb.toString();
                sb.append(") ");
                if (len > 3) sb.append(d, 3, Math.min(6, len));
                if (len < 6) return sb.toString();
                sb.append("-");
                if (len > 6) sb.append(d, 6, Math.min(8, len));
                if (len < 8) return sb.toString();
                sb.append("-");
                if (len > 8) sb.append(d, 8, len);
                return sb.toString();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up_completion3rd_party, container, false);
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

        MaterialCardView phoneCard = view.findViewById(R.id.materialCardView3);
        TextView tvPhoneLabel = view.findViewById(R.id.tvPhoneLabel);
        EditText etPhone = view.findViewById(R.id.etPhone);
        setupPhoneFormatting(etPhone);
        TextView tvConfirm = view.findViewById(R.id.tvConfirm);
        View phoneDivider = view.findViewById(R.id.phoneDivider);
        LinearLayout otpRow = view.findViewById(R.id.otpRow);

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && tvPhoneLabel.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(phoneCard, new AutoTransition());
                tvPhoneLabel.setVisibility(View.GONE);
                tvConfirm.setVisibility(View.VISIBLE);
            }
        });

        tvConfirm.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(phoneCard, new AutoTransition());
            phoneDivider.setVisibility(View.VISIBLE);
            otpRow.setVisibility(View.VISIBLE);
        });
    }
}
