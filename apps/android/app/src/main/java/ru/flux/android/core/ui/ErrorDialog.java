package ru.flux.android.core.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import ru.flux.android.R;
import ru.flux.android.core.views.BaseBlurView;

public class ErrorDialog extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static void display(FragmentManager fm, String message) {
        ErrorDialog d = new ErrorDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        d.setArguments(args);
        d.show(fm, "error_dialog");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_error, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE, "") : "";
        TextView tvMessage = view.findViewById(R.id.tv_error_message);
        tvMessage.setText(message);

        view.findViewById(R.id.btn_continue).setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Re-attach blur to activity root so it captures content behind the dialog window
        View root = getView();
        if (root == null) return;
        BaseBlurView blurView = root.findViewById(R.id.blur_container);
        if (blurView == null) return;
        ViewGroup activityRoot = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Drawable windowBg = requireActivity().getWindow().getDecorView().getBackground();
        blurView.setupWith(activityRoot)
                .setFrameClearDrawable(windowBg)
                .setBlurRadius(13f);
    }
}