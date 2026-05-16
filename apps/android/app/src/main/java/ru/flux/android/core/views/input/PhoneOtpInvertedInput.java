package ru.flux.android.core.views.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import ru.flux.android.R;
import ru.flux.android.core.ui.PhoneTextWatcher;
import ru.flux.android.databinding.PhoneOtpInvertedInputViewBinding;

public class PhoneOtpInvertedInput extends BaseInvertedInput {
    private PhoneOtpInvertedInputViewBinding binding;

    public PhoneOtpInvertedInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = PhoneOtpInvertedInputViewBinding.bind(this);

        setLabel(R.string.prompt_phone);
        binding.baseInput.setHint("+7");
        binding.tvConfirm.setText(R.string.phone_input_confirm);
        binding.otpLabel.setText(R.string.phone_input_otp_label);
        binding.otpInput.setHint(R.string.phone_input_otp_hint);
        PhoneTextWatcher.setup(binding.baseInput);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PhoneOtpInvertedInput);
            CharSequence phoneLabel = a.getText(R.styleable.PhoneOtpInvertedInput_phoneLabel);
            CharSequence phoneHint  = a.getText(R.styleable.PhoneOtpInvertedInput_phoneHint);
            CharSequence confirmText = a.getText(R.styleable.PhoneOtpInvertedInput_confirmText);
            CharSequence otpLabel   = a.getText(R.styleable.PhoneOtpInvertedInput_otpLabel);
            CharSequence otpHint    = a.getText(R.styleable.PhoneOtpInvertedInput_otpHint);
            a.recycle();

            if (phoneLabel != null) binding.baseLabel.setText(phoneLabel);
            if (phoneHint != null)  binding.baseInput.setHint(phoneHint);
            if (confirmText != null) binding.tvConfirm.setText(confirmText);
            if (otpLabel != null)   binding.otpLabel.setText(otpLabel);
            if (otpHint != null)    binding.otpInput.setHint(otpHint);
        }

        configureOtpMode();
    }

    public String getPhone() {
        return PhoneTextWatcher.normalize(binding.baseInput.getText().toString());
    }

    public String getOtp() {
        return binding.otpInput.getText() != null
                ? binding.otpInput.getText().toString().trim()
                : "";
    }

    private void configureOtpMode() {
        binding.tvConfirm.setVisibility(View.GONE);
        binding.phoneDivider.setVisibility(View.GONE);
        binding.otpRow.setVisibility(View.GONE);

        binding.baseInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && binding.baseLabel.getVisibility() == View.VISIBLE) {
                TransitionManager.beginDelayedTransition(this, new AutoTransition());
                binding.baseLabel.setVisibility(View.GONE);
                binding.tvConfirm.setVisibility(View.VISIBLE);
            }
        });

        binding.tvConfirm.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition(this, new AutoTransition());
            binding.phoneDivider.setVisibility(View.VISIBLE);
            binding.otpRow.setVisibility(View.VISIBLE);
        });
    }
}