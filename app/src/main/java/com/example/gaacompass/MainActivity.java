package com.example.gaacompass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private View headerBackground;
    private TextView statStreakValue;
    private TextView statAccuracyValue;
    private TextView statRecoveryValue;
    private TextView txtRecentFreesTime;
    private TextView txtRecentFreesAccuracy;
    private TextView txtRecentFreesAttempts;
    private TextView txtRecentCheckinTime;
    private TextView txtRecentCheckinScore;
    private TextView txtMotivationBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        headerBackground = findViewById(R.id.header_container);
        statStreakValue = findViewById(R.id.stat_streak_value);
        statAccuracyValue = findViewById(R.id.stat_accuracy_value);
        statRecoveryValue = findViewById(R.id.stat_recovery_value);
        txtRecentFreesTime = findViewById(R.id.txt_recent_frees_time);
        txtRecentFreesAccuracy = findViewById(R.id.txt_recent_frees_accuracy);
        txtRecentFreesAttempts = findViewById(R.id.txt_recent_frees_attempts);
        txtRecentCheckinTime = findViewById(R.id.txt_recent_checkin_time);
        txtRecentCheckinScore = findViewById(R.id.txt_recent_checkin_score);
        txtMotivationBody = findViewById(R.id.txt_motivation_body);
        ThemePrefs.applyHeaderTheme(this, headerBackground);

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
        ThemePrefs.applyHeaderTheme(this, headerBackground);
        renderHomeSummary();
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        findViewById(R.id.btn_log_frees).setOnClickListener(v -> {
            startActivity(new Intent(this, LogFreesActivity.class));
        });

        findViewById(R.id.btn_weekly_plan).setOnClickListener(v -> {
            startActivity(new Intent(this, WeeklyPlannerActivity.class));
        });

        findViewById(R.id.btn_daily_checkin).setOnClickListener(v -> {
            startActivity(new Intent(this, DailyCheckInActivity.class));
        });

        findViewById(R.id.btn_progress).setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressActivity.class));
        });

        findViewById(R.id.recent_sessions_card).setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressActivity.class));
        });
    }

    private void renderHomeSummary() {
        AnalyticsStore.Summary summary = new AnalyticsStore(this).getSummary();
        statStreakValue.setText(String.valueOf(summary.currentStreak));
        statAccuracyValue.setText(getString(R.string.percent_format, summary.weightedAverageAccuracy));
        statRecoveryValue.setText(String.format(Locale.getDefault(), "%.1f", summary.latestCheckin == null ? 0f : summary.latestCheckin.recovery));

        if (summary.latestSession == null) {
            txtRecentFreesTime.setText(R.string.no_sessions_yet);
            txtRecentFreesAccuracy.setText(R.string.acc_zero);
            txtRecentFreesAttempts.setText(R.string.recent_no_attempts);
        } else {
            txtRecentFreesTime.setText(relativeTime(summary.latestSession.timestamp));
            txtRecentFreesAccuracy.setText(getString(R.string.percent_format, summary.latestSession.accuracy));
            txtRecentFreesAttempts.setText(getString(R.string.attempts_format, summary.latestSession.attempts));
        }

        if (summary.latestCheckin == null) {
            txtRecentCheckinTime.setText(R.string.no_checkins_yet);
            txtRecentCheckinScore.setText(R.string.recovery_zero_ten);
        } else {
            txtRecentCheckinTime.setText(relativeTime(summary.latestCheckin.timestamp));
            txtRecentCheckinScore.setText(getString(R.string.recovery_of_ten, summary.latestCheckin.recovery));
        }

        txtMotivationBody.setText(getString(R.string.motivation_banner_dynamic, summary.currentStreak));
    }

    private String relativeTime(long timestamp) {
        long diffMs = System.currentTimeMillis() - timestamp;
        if (diffMs < 60L * 60L * 1000L) {
            long mins = Math.max(1, diffMs / (60L * 1000L));
            return getString(R.string.minutes_ago, mins);
        }
        if (diffMs < 24L * 60L * 60L * 1000L) {
            long hours = Math.max(1, diffMs / (60L * 60L * 1000L));
            return getString(R.string.hours_ago, hours);
        }
        long days = Math.max(1, diffMs / (24L * 60L * 60L * 1000L));
        return getString(R.string.days_ago, days);
    }
}
