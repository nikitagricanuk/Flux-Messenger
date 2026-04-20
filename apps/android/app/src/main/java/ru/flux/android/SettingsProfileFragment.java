package ru.flux.android;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.util.Calendar;

import eightbitlab.com.blurview.BlurView;

public class SettingsProfileFragment extends Fragment {

    public SettingsProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupBlurViews(view);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        setupBirthDate(view);
        setupEditableRow(view, R.id.rowUsername, R.id.etUsername);
        setupEditableRow(view, R.id.rowPhone, R.id.etPhone);
        setupEditableRow(view, R.id.rowEmail, R.id.etEmail);
    }

    private void setupBlurViews(View root) {
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();

        int[] ids = { R.id.blurCardName, R.id.blurCardBio, R.id.cardBirthDate,
                      R.id.cardAccountInfo, R.id.cardActions };
        for (int id : ids) {
            BlurView bv = root.findViewById(id);
            if (bv == null) continue;
            bv.setClipToOutline(true);
            bv.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(20f);
        }
    }

    private void setupBirthDate(View root) {
        BlurView card = root.findViewById(R.id.cardBirthDate);
        TextView tvBirthDate = root.findViewById(R.id.tvBirthDate);
        View divider = root.findViewById(R.id.birthDateDivider);
        TextView tvDelete = root.findViewById(R.id.tvDeleteBirthDate);

        root.findViewById(R.id.rowBirthDate).setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                tvBirthDate.setText(String.format("%02d.%02d.%d", day, month + 1, year));
                TransitionManager.beginDelayedTransition(card, new AutoTransition());
                divider.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvDelete.setOnClickListener(v -> {
            tvBirthDate.setText("");
            TransitionManager.beginDelayedTransition(card, new AutoTransition());
            divider.setVisibility(View.GONE);
            tvDelete.setVisibility(View.GONE);
        });
    }

    private void setupEditableRow(View root, int rowId, int editTextId) {
        EditText et = root.findViewById(editTextId);
        root.findViewById(rowId).setOnClickListener(v -> {
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setSelection(et.getText().length());
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
