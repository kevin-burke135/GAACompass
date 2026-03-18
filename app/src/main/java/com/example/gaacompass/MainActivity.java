package com.example.gaacompass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private View headerBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        headerBackground = findViewById(R.id.header_container);
        applyHeaderTheme();

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyHeaderTheme();
    }

    private void applyHeaderTheme() {
        if (headerBackground == null) return;
        String themeKey = new ThemePrefs(this).getThemeKey();
        int drawableRes = ThemePrefs.getHeaderDrawableForTheme(themeKey);
        headerBackground.setBackgroundResource(drawableRes);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        findViewById(R.id.btn_log_frees).setOnClickListener(v -> {
            startActivity(new Intent(this, LogFreesActivity.class));
        });

        findViewById(R.id.btn_weekly_plan).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.weekly_plan) + " — coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_daily_checkin).setOnClickListener(v -> {
            startActivity(new Intent(this, DailyCheckInActivity.class));
        });

        findViewById(R.id.btn_progress).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.progress) + " — coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}
