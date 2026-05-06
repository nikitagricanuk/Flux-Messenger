package ru.flux.android.core.views.input;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import ru.flux.android.core.ui.PhoneTextWatcher;

public class PhoneInvertedInput extends BaseInvertedInput {
    public PhoneInvertedInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        PhoneTextWatcher.setup(binding.baseInput);
    }

    public String getPhone() {
        return PhoneTextWatcher.normalize(binding.baseInput.getText().toString());
    }
}
