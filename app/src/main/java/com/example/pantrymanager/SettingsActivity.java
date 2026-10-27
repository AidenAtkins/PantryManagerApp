package com.example.pantrymanager;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Settings screen from the brief. Holds one preference: whether expired pantry
 * items are ignored when suggesting recipes. The value is saved immediately
 * through AppSettings (SharedPreferences) and read by SuggestedRecipesActivity.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SwitchCompat ignoreExpiredSwitch = findViewById(R.id.switch_ignore_expired);
        ignoreExpiredSwitch.setChecked(AppSettings.isIgnoreExpiredEnabled(this));
        ignoreExpiredSwitch.setOnCheckedChangeListener((button, isChecked) ->
                AppSettings.setIgnoreExpiredEnabled(this, isChecked));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        BottomNavHelper.setup(this, bottomNav, R.id.nav_settings);
    }
}