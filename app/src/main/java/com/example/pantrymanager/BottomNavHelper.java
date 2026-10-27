package com.example.pantrymanager;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires up the bottom navigation bar shared by the three main screens, so the
 * navigation code lives in one place instead of being copied into every Activity.
 *
 * Pantry is the root screen. The other tabs sit directly on top of it, so the back
 * stack is always [Pantry, current tab].
 */
public final class BottomNavHelper {

    private BottomNavHelper() {
        // Utility class: no instances.
    }

    public static void setup(Activity activity, BottomNavigationView nav, int selectedItemId) {
        // Highlight this screen's own tab (done before the listener exists, so nothing fires).
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(activity, MainActivity.class);
                // Return to the existing Pantry screen instead of stacking a new one.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_recipes) {
                intent = new Intent(activity, SuggestedRecipesActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            } else {
                return false;
            }

            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0); // no slide, so it feels like a tab switch

            // Leaving a non-root tab closes it, so tabs never pile up on the back stack.
            if (selectedItemId != R.id.nav_pantry) {
                activity.finish();
            }

            // false = keep this screen's own tab highlighted (the new screen highlights its own).
            return false;
        });
    }
}