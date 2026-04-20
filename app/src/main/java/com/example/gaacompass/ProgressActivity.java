package com.example.gaacompass;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private TextView txtCurrentStreak;
    private TextView txtBestStreak;
    private TextView txtAcc7;
    private TextView txtAcc30;
    private TextView txtAccAll;
    private TextView txtVolumeThisWeek;
    private TextView txtVolumeLastWeek;
    private TextView txtRecoveryThisWeek;
    private TextView txtRecoveryLastWeek;
    private TextView txtTotalSessions;
    private TextView txtTotalAttempts;
    private TextView txtAvgAccuracy;
    private TextView txtBestAccuracy;
    private LinearLayout listRecentSessions;
    private LinearLayout listRecentCheckins;
    private android.view.View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        headerView = findViewById(R.id.header);
        ThemePrefs.applyHeaderTheme(this, headerView);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        txtCurrentStreak = findViewById(R.id.txt_current_streak);
        txtBestStreak = findViewById(R.id.txt_best_streak);
        txtAcc7 = findViewById(R.id.txt_acc_7);
        txtAcc30 = findViewById(R.id.txt_acc_30);
        txtAccAll = findViewById(R.id.txt_acc_all);
        txtVolumeThisWeek = findViewById(R.id.txt_volume_this_week);
        txtVolumeLastWeek = findViewById(R.id.txt_volume_last_week);
        txtRecoveryThisWeek = findViewById(R.id.txt_recovery_this_week);
        txtRecoveryLastWeek = findViewById(R.id.txt_recovery_last_week);
        txtTotalSessions = findViewById(R.id.txt_total_sessions);
        txtTotalAttempts = findViewById(R.id.txt_total_attempts);
        txtAvgAccuracy = findViewById(R.id.txt_avg_accuracy);
        txtBestAccuracy = findViewById(R.id.txt_best_accuracy);
        listRecentSessions = findViewById(R.id.list_recent_sessions);
        listRecentCheckins = findViewById(R.id.list_recent_checkins);

        renderAnalytics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemePrefs.applyHeaderTheme(this, headerView);
        renderAnalytics();
    }

    private void renderAnalytics() {
        AnalyticsStore.Summary summary = new AnalyticsStore(this).getSummary();
        txtCurrentStreak.setText(String.valueOf(summary.currentStreak));
        txtBestStreak.setText(String.valueOf(summary.bestStreak));
        txtAcc7.setText(formatPercent(summary.accuracy7Days));
        txtAcc30.setText(formatPercent(summary.accuracy30Days));
        txtAccAll.setText(formatPercent(summary.accuracyAllTime));
        txtVolumeThisWeek.setText(String.valueOf(summary.attemptsThisWeek));
        txtVolumeLastWeek.setText(String.valueOf(summary.attemptsLastWeek));
        txtRecoveryThisWeek.setText(formatOneDecimal(summary.recoveryThisWeek));
        txtRecoveryLastWeek.setText(formatOneDecimal(summary.recoveryLastWeek));
        txtTotalSessions.setText(String.valueOf(summary.sessions.size()));
        txtTotalAttempts.setText(String.valueOf(summary.totalAttempts));
        txtAvgAccuracy.setText(formatPercent(summary.weightedAverageAccuracy));
        txtBestAccuracy.setText(formatPercent(summary.bestAccuracy));
        renderRecentSessions(summary.sessions);
        renderRecentCheckins(summary.checkins);
    }

    private void renderRecentSessions(List<AnalyticsStore.SessionEntry> sessions) {
        listRecentSessions.removeAllViews();
        int count = Math.min(7, sessions.size());
        for (int i = 0; i < count; i++) {
            AnalyticsStore.SessionEntry s = sessions.get(i);
            String date = formatDate(s.timestamp);
            String right = s.accuracy + "%";
            String sub = s.attempts + " " + getString(R.string.attempts_plain);
            listRecentSessions.addView(buildRow(date, sub, right));
        }
        if (count == 0) {
            listRecentSessions.addView(buildSingleText(getString(R.string.no_sessions_yet)));
        }
    }

    private void renderRecentCheckins(List<AnalyticsStore.CheckInEntry> checkins) {
        listRecentCheckins.removeAllViews();
        int count = Math.min(7, checkins.size());
        for (int i = 0; i < count; i++) {
            AnalyticsStore.CheckInEntry c = checkins.get(i);
            String date = formatDate(c.timestamp);
            String right = formatOneDecimal(c.recovery);
            listRecentCheckins.addView(buildRow(date, getString(R.string.recovery_label), right));
        }
        if (count == 0) {
            listRecentCheckins.addView(buildSingleText(getString(R.string.no_checkins_yet)));
        }
    }

    private TextView buildSingleText(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tv.setPadding(0, 8, 0, 0);
        return tv;
    }

    private LinearLayout buildRow(String leftTop, String leftBottom, String rightText) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 6, 0, 6);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        left.setLayoutParams(lpLeft);

        TextView t1 = new TextView(this);
        t1.setText(leftTop);
        t1.setTextSize(12f);
        t1.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        TextView t2 = new TextView(this);
        t2.setText(leftBottom);
        t2.setTextSize(11f);
        t2.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        left.addView(t1);
        left.addView(t2);

        TextView right = new TextView(this);
        right.setText(rightText);
        right.setTextSize(12f);
        right.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        row.addView(left);
        row.addView(right);
        return row;
    }

    private String formatDate(long ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(ts);
    }

    private String formatPercent(int value) {
        return value + "%";
    }

    private String formatOneDecimal(float value) {
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}

