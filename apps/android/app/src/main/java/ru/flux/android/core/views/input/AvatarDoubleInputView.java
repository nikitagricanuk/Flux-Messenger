package ru.flux.android.core.views.input;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import ru.flux.android.R;

public class AvatarDoubleInputView extends BaseDoubleInputView {
    private ImageView avatarView;

    public AvatarDoubleInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        restructure(context);
        applyCardStyle(context);
    }

    private void restructure(Context context) {
        // Grab the 3 children inflated by super: [tilFirst, divider, tilSecond]
        View tilFirst = getChildAt(0);
        View divider = getChildAt(1);
        View tilSecond = getChildAt(2);
        removeAllViews();

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int p = dp(context, 12);
        setPadding(p, p, p, p);

        // Avatar container
        int avatarSize = dp(context, 64);
        avatarView = new ImageView(context);
        avatarView.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_circle_gray));
        avatarView.setScaleType(ImageView.ScaleType.CENTER);
        avatarView.setImageResource(R.drawable.ic_camera_placeholder);
        int avatarPad = dp(context, 20);
        avatarView.setPadding(avatarPad, avatarPad, avatarPad, avatarPad);

        FrameLayout avatarContainer = new FrameLayout(context);
        avatarContainer.addView(avatarView, new FrameLayout.LayoutParams(avatarSize, avatarSize));

        LinearLayout.LayoutParams containerLP = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        containerLP.setMarginEnd(dp(context, 12));
        addView(avatarContainer, containerLP);

        // Fields container — rewraps the inherited children in a new vertical layout
        LinearLayout fieldsContainer = new LinearLayout(context);
        fieldsContainer.setOrientation(VERTICAL);
        fieldsContainer.addView(tilFirst);
        fieldsContainer.addView(divider);
        fieldsContainer.addView(tilSecond);

        LinearLayout.LayoutParams fieldsLP =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        fieldsLP.gravity = Gravity.CENTER_VERTICAL;
        addView(fieldsContainer, fieldsLP);
    }

    private void applyCardStyle(Context context) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(context, 16));
        setBackground(bg);
        setElevation(dp(context, 4));
    }

    public ImageView getAvatarView() {
        return avatarView;
    }

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}