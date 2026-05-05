package ru.flux.android.core.views.input;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;

import ru.flux.android.R;

public class BaseIconInputView extends BaseInputView {

    public BaseIconInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding.baseEdit.setCompoundDrawablePadding(
                (int) getResources().getDimension(R.dimen.input_drawable_padding));
        binding.baseEdit.setPaddingRelative(
                (int) getResources().getDimension(R.dimen.input_padding_start),
                binding.baseEdit.getPaddingTop(),
                binding.baseEdit.getPaddingEnd(),
                binding.baseEdit.getPaddingBottom());
    }

    protected void setIcon(@DrawableRes int drawableRes) {
        Drawable icon = AppCompatResources.getDrawable(getContext(), drawableRes);
        binding.baseEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
    }
}
