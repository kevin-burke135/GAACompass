package com.example.gaacompass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LogFreesActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "gaacompass_prefs";
    private static final String KEY_FREES_SESSIONS = "frees_sessions";

    private final List<FreeAttempt> attempts = new ArrayList<>();
    private GaaPitchView pitchView;
    private TextView statAttemptsValue;
    private TextView statScoredValue;
    private TextView statAccValue;
    private android.view.View headerView;
    private boolean skipUnsavedPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_frees);

        pitchView = findViewById(R.id.gaa_pitch);
        statAttemptsValue = findViewById(R.id.stat_attempts_value);
        statScoredValue = findViewById(R.id.stat_scored_value);
        statAccValue = findViewById(R.id.stat_acc_value);
        headerView = findViewById(R.id.header);
        ThemePrefs.applyHeaderTheme(this, headerView);

        findViewById(R.id.btn_back).setOnClickListener(v -> attemptExit());
        findViewById(R.id.btn_clear_all).setOnClickListener(v -> showClearAllConfirm());
        findViewById(R.id.btn_end_session).setOnClickListener(v -> showEndSessionSummary());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                attemptExit();
            }
        });

        pitchView.setAttempts(attempts);
        pitchView.setOnPitchTapListener((normX, normY) -> showAttemptDialog(normX, normY));
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemePrefs.applyHeaderTheme(this, headerView);
    }

    private void showAttemptDialog(float normX, float normY) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.record_attempt_prompt))
                .setPositiveButton(getString(R.string.scored), (dialog, which) -> addAttempt(normX, normY, true))
                .setNegativeButton(getString(R.string.wide), (dialog, which) -> addAttempt(normX, normY, false))
                .setNeutralButton(getString(R.string.cancel), null)
                .show();
    }

    private void addAttempt(float normX, float normY, boolean scored) {
        attempts.add(new FreeAttempt(normX, normY, scored, System.currentTimeMillis()));
        pitchView.setAttempts(attempts);
        updateStats();
    }

    private void updateStats() {
        int total = attempts.size();
        int scored = 0;
        for (FreeAttempt a : attempts) {
            if (a.scored) scored++;
        }
        statAttemptsValue.setText(String.valueOf(total));
        statScoredValue.setText(String.valueOf(scored));
        if (total == 0) {
            statAccValue.setText(getString(R.string.acc_zero));
        } else {
            int pct = Math.round(100f * scored / total);
            statAccValue.setText(pct + "%");
        }
    }

    private void showClearAllConfirm() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_all_confirm_title)
                .setMessage(R.string.clear_all_confirm_message)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    attempts.clear();
                    pitchView.setAttempts(attempts);
                    updateStats();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEndSessionSummary() {
        int total = attempts.size();
        int scored = 0;
        for (FreeAttempt a : attempts) {
            if (a.scored) scored++;
        }
        int acc = total == 0 ? 0 : Math.round(100f * scored / total);

        StringBuilder details = new StringBuilder();
        details.append(getString(R.string.total_attempts, total)).append("\n");
        details.append(getString(R.string.scored_count, scored)).append("\n");
        details.append(getString(R.string.accuracy_percent, acc)).append("\n\n");
        int missed = total - scored;
        if (total > 0) {
            details.append(scored).append(" ").append(getString(R.string.scored).toLowerCase());
            details.append(", ").append(missed).append(" ").append(getString(R.string.wide).toLowerCase());
        }

        final int totalFinal = total;
        final int scoredFinal = scored;
        final int accFinal = acc;
        new AlertDialog.Builder(this)
                .setTitle(R.string.session_complete)
                .setMessage(details.toString())
                .setPositiveButton(R.string.save_session, (dialog, which) ->
                        saveSessionAndFinish(totalFinal, scoredFinal, accFinal))
                .setNegativeButton(R.string.discard, (dialog, which) -> {
                    skipUnsavedPrompt = true;
                    finish();
                })
                .show();
    }

    private void saveSessionAndFinish(int total, int scored, int accuracy) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String existing = prefs.getString(KEY_FREES_SESSIONS, "[]");
        try {
            JSONArray arr = new JSONArray(existing);
            JSONObject session = new JSONObject();
            session.put("timestamp", System.currentTimeMillis());
            session.put("attempts", total);
            session.put("scored", scored);
            session.put("accuracy", accuracy);
            arr.put(session);
            prefs.edit().putString(KEY_FREES_SESSIONS, arr.toString()).apply();
        } catch (JSONException e) {
            JSONArray arr = new JSONArray();
            try {
                arr.put(new JSONObject().put("timestamp", System.currentTimeMillis()).put("attempts", total).put("scored", scored).put("accuracy", accuracy));
                prefs.edit().putString(KEY_FREES_SESSIONS, arr.toString()).apply();
            } catch (JSONException ignored) { }
        }
        Toast.makeText(this, getString(R.string.save_session), Toast.LENGTH_SHORT).show();
        skipUnsavedPrompt = true;
        setResult(RESULT_OK);
        finish();
    }

    private void attemptExit() {
        if (skipUnsavedPrompt || attempts.isEmpty()) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.discard_session_title)
                .setMessage(R.string.discard_session_message)
                .setPositiveButton(R.string.discard, (dialog, which) -> {
                    skipUnsavedPrompt = true;
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
