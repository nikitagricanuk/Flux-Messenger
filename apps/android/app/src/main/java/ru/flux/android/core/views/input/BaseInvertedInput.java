package ru.flux.android.core.views.input;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import ru.flux.android.R;
import ru.flux.android.core.views.BaseBlurView;
import ru.flux.android.databinding.BaseInvertedInputViewBinding;

public class BaseInvertedInput extends BaseBlurView {
    BaseInvertedInputViewBinding binding;

    public BaseInvertedInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = BaseInvertedInputViewBinding.inflate(LayoutInflater.from(context), this);
    }

    public void setLabel(int resid) {
        binding.baseLabel.setText(resid);
    }

    public void setInput(int resid) {
        binding.baseInput.setText(resid);
    }

    public Editable getInput() {
         return binding.baseInput.getText();
    }

    public void addTextChangedListener(TextWatcher watcher) {
        binding.baseInput.addTextChangedListener(watcher);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        binding.baseInput.setOnEditorActionListener(listener);
    }
}
