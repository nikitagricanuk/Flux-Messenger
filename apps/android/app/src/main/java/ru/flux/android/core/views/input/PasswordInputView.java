package ru.flux.android.core.views.input;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import ru.flux.android.R;

public class PasswordInputView extends BaseIconInputView {
    public PasswordInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIcon(R.drawable.lock_1);
        binding.baseEdit.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        binding.baseEdit.setHint(R.string.prompt_password);
        binding.baseEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }
}
