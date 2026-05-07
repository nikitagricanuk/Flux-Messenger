package ru.flux.android.core.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;

public class SegmentTabsView extends LinearLayout {

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    private final List<TextView> tabs = new ArrayList<>();
    private OnTabSelectedListener listener;

    public SegmentTabsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        int p = dpToPx(4);
        setPadding(p, p, p, p);
        setBackground(ContextCompat.getDrawable(context, R.drawable.bg_segment_container));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SegmentTabsView);
        int resId = a.getResourceId(R.styleable.SegmentTabsView_tabs, 0);
        a.recycle();

        if (resId != 0) {
            setTabs(context.getResources().getStringArray(resId));
        }
    }

    public void setTabs(String... labels) {
        removeAllViews();
        tabs.clear();
        for (int i = 0; i < labels.length; i++) {
            TextView tab = buildTab(labels[i], i);
            tabs.add(tab);
            addView(tab);
        }
        if (!tabs.isEmpty()) {
            setActiveTab(0);
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setActiveTab(int index) {
        for (int i = 0; i < tabs.size(); i++) {
            tabs.get(i).setBackgroundResource(i == index ? R.drawable.bg_segment_selected : 0);
        }
    }

    private TextView buildTab(String label, int index) {
        AppCompatTextView tv = new AppCompatTextView(getContext());
        LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        tv.setLayoutParams(params);
        tv.setGravity(Gravity.CENTER);
        tv.setText(label);
        TextViewCompat.setTextAppearance(tv, R.style.tabs);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setOnClickListener(v -> {
            setActiveTab(index);
            if (listener != null) listener.onTabSelected(index);
        });
        return tv;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
