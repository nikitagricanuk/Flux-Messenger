package ru.flux.android.core.views.input;

import android.content.Context;
import android.util.AttributeSet;

import ru.flux.android.R;
import ru.flux.android.core.ui.PhoneTextWatcher;

public class PhoneInputView extends BaseIconInputView {

    public PhoneInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIcon(R.drawable.phone_rotary_1);
        PhoneTextWatcher.setup(binding.baseEdit);
    }

    public String getPhone() {
        return PhoneTextWatcher.normalize(binding.baseEdit.getText().toString());
    }
}
