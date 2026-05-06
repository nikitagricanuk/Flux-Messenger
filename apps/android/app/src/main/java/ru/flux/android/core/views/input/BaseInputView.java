package ru.flux.android.core.views.input;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ru.flux.android.core.views.BaseBlurView;
import ru.flux.android.databinding.BaseInputViewBinding;

public class BaseInputView extends BaseBlurView {
    protected BaseInputViewBinding binding;

    public BaseInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = BaseInputViewBinding.inflate(LayoutInflater.from(context), this);
    }

    public Editable getText() {
        return binding.baseEdit.getText();
    }

    public void addTextChangedListener(TextWatcher watcher) {
        binding.baseEdit.addTextChangedListener(watcher);
    }

    public void setError(CharSequence error) {
        binding.baseEdit.setError(error);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        binding.baseEdit.setOnEditorActionListener(listener);
    }
}
