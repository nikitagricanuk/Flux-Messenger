package ru.flux.android.core.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import eightbitlab.com.blurview.BlurView;
import ru.flux.android.R;

public class BaseBlurView extends BlurView {
    private float blurRadius;
    private boolean clipToOutline;
    private boolean blurConfigured;

    public BaseBlurView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        blurRadius = dpToPx(20f);
        clipToOutline = true;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseBlurView);
            blurRadius = a.getDimension(R.styleable.BaseBlurView_blurRadius, blurRadius);
            clipToOutline = a.getBoolean(R.styleable.BaseBlurView_blurClipToOutline, true);
            a.recycle();
        }

        setClipToOutline(clipToOutline);
    }

    public BaseBlurView(Context context) {
        this(context, null);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupBlurIfNeeded();
    }

    @Override
    protected void onDetachedFromWindow() {
        blurConfigured = false;
        super.onDetachedFromWindow();
    }

    private void setupBlurIfNeeded() {
        if (blurConfigured) return;

        View root = getRootView();
        if (!(root instanceof ViewGroup)) {
            post(this::setupBlurIfNeeded);
            return;
        }

        Drawable windowBackground = null;
        Context context = getContext();
        if (context instanceof Activity) {
            windowBackground = ((Activity) context).getWindow().getDecorView().getBackground();
        }
        if (windowBackground == null) {
            windowBackground = root.getBackground();
        }

        setupWith((ViewGroup) root)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(blurRadius);

        blurConfigured = true;
    }

    private float dpToPx(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
