package com.example.pantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Reads and writes the app's user settings using SharedPreferences, a small
 * key-value store that persists between app launches.
 */
public final class AppSettings {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired_items";

    private AppSettings() {
        // Utility class: no instances.
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Off by default, so the app behaves as before until the user turns it on.
    public static boolean isIgnoreExpiredEnabled(Context context) {
        return prefs(context).getBoolean(KEY_IGNORE_EXPIRED, false);
    }

    public static void setIgnoreExpiredEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_IGNORE_EXPIRED, enabled).apply();
    }
}