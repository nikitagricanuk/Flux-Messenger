package ru.flux.android.core.views.input;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import ru.flux.android.R;

public class AvatarSingleInputView extends BaseInputView {
    private ImageView avatarView;

    public AvatarSingleInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        restructure(context);
        if (getBackground() == null) applyWhiteCardStyle(context);
    }

    private void restructure(Context context) {
        // super() inflated base_input_view.xml: single EditText added to this FrameLayout.
        // Detach it, then re-attach inside a horizontal row with an avatar on the left.
        removeAllViews();

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        int p = dp(context, 12);
        content.setPadding(p, p, p, p);

        int avatarSize = dp(context, 64);
        avatarView = new ImageView(context);
        avatarView.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_circle_gray));
        avatarView.setScaleType(ImageView.ScaleType.CENTER);
        avatarView.setImageResource(R.drawable.ic_camera_placeholder);
        avatarView.setId(R.id.avatar);

        FrameLayout avatarContainer = new FrameLayout(context);
        avatarContainer.addView(avatarView, new FrameLayout.LayoutParams(avatarSize, avatarSize));

        LinearLayout.LayoutParams avatarLP = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        avatarLP.setMarginEnd(dp(context, 12));
        content.addView(avatarContainer, avatarLP);

        LinearLayout.LayoutParams editLP =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        editLP.gravity = Gravity.CENTER_VERTICAL;
        content.addView(binding.baseEdit, editLP);

        addView(content, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, dp(context, 92)));
    }

    private void applyWhiteCardStyle(Context context) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(context, 16));
        setBackground(bg);
        setElevation(dp(context, 4));
    }

    public ImageView getAvatar() {
        return avatarView;
    }

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}