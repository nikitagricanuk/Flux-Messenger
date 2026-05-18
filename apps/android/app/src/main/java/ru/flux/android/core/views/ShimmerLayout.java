package ru.flux.android.core.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

public class ShimmerLayout extends FrameLayout {

    private final Paint shimmerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator animator;
    private float progress = 0f;

    public ShimmerLayout(Context context) {
        super(context);
        init();
    }

    public ShimmerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShimmerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        shimmerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startShimmer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }

    private void startShimmer() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1400);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopShimmer() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        super.dispatchDraw(canvas);

        int w = getWidth();
        float highlightWidth = w * 0.45f;
        float start = progress * (w + highlightWidth) - highlightWidth;

        shimmerPaint.setShader(new LinearGradient(
                start, 0, start + highlightWidth, 0,
                new int[]{0x00FFFFFF, 0x80FFFFFF, 0x00FFFFFF},
                null,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, w, getHeight(), shimmerPaint);
        canvas.restoreToCount(count);
    }
}