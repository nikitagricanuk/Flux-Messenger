package ru.flux.android.core.ui;

public class AvatarColorHelper {
    private static final int[] COLORS = {
            0xFF1ABC9C, 0xFF2ECC71, 0xFF3498DB, 0xFF9B59B6,
            0xFFE67E22, 0xFFE74C3C, 0xFF1A7FC1, 0xFF2C3E50
    };

    // Same name always gets the same color
    public static int colorFor(String name) {
        return COLORS[Math.abs(name.hashCode()) % COLORS.length];
    }
}
