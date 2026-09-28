package com.example.foodapplication.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InsetUtils {

    /**
     * Applies system bar insets (status bar top padding & navigation bar bottom padding)
     * so that layout content does not overlap with notch, status bar, or bottom gesture bar / curved screen edges.
     */
    public static void applySystemBarInsets(View rootView) {
        if (rootView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top + 8, // Safety margin for top notch/curved screen
                    v.getPaddingRight(),
                    systemBars.bottom + 8 // Safety margin for bottom gesture bar/curved screen
            );
            return insets;
        });
    }

    public static void applyTopInsetOnly(View view) {
        if (view == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });
    }
}
