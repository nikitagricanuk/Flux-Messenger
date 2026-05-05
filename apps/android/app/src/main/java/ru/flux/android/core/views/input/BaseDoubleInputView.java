package ru.flux.android.core.views.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import ru.flux.android.R;
import ru.flux.android.databinding.BaseDoubleInputViewBinding;

public class BaseDoubleInputView extends LinearLayout {
    protected BaseDoubleInputViewBinding binding;

    public BaseDoubleInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        binding = BaseDoubleInputViewBinding.inflate(LayoutInflater.from(context), this);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseDoubleInputView);
            String firstHint = a.getString(R.styleable.BaseDoubleInputView_firstHint);
            String secondHint = a.getString(R.styleable.BaseDoubleInputView_secondHint);
            a.recycle();
            if (firstHint != null) binding.etFirst.setHint(firstHint);
            if (secondHint != null) binding.etSecond.setHint(secondHint);
        }
    }

    public Editable getFirstText() {
        return binding.etFirst.getText();
    }

    public Editable getSecondText() {
        return binding.etSecond.getText();
    }

    public void addFirstTextChangedListener(TextWatcher watcher) {
        binding.etFirst.addTextChangedListener(watcher);
    }

    public void addSecondTextChangedListener(TextWatcher watcher) {
        binding.etSecond.addTextChangedListener(watcher);
    }

    public void setFirstError(CharSequence error) {
        binding.tilFirst.setError(error);
    }

    public void setSecondError(CharSequence error) {
        binding.tilSecond.setError(error);
    }
}