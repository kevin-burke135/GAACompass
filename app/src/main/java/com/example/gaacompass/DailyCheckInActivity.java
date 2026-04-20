package com.example.gaacompass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyCheckInActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "gaacompass_prefs";

    private static final String KEY_SLEEP = "daily_sleep";
    private static final String KEY_ENERGY = "daily_energy";
    private static final String KEY_SORENESS = "daily_soreness";
    private static final String KEY_STRESS = "daily_stress";
    private static final String KEY_RPE = "daily_rpe";
    private static final String KEY_RECOVERY = "daily_recovery";
    private static final String KEY_TIMESTAMP = "daily_checkin_timestamp";
    private static final String KEY_CHECKIN_HISTORY = "daily_checkins_history";

    private SeekBar seekSleep;
    private SeekBar seekEnergy;
    private SeekBar seekSoreness;
    private SeekBar seekStress;
    private SeekBar seekRpe;

    private TextView txtSleepValue;
    private TextView txtSleepLabel;

    private TextView txtEnergyValue;
    private TextView txtEnergyLabel;

    private TextView txtSorenessValue;
    private TextView txtSorenessLabel;

    private TextView txtStressValue;
    private TextView txtStressLabel;

    private TextView txtRpeValue;
    private TextView txtRpeLabel;

    private TextView txtRecoveryValue;
    private TextView txtRecommendation;

    private TextView txtDate;
    private android.view.View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_checkin);

        headerView = findViewById(R.id.header);
        ThemePrefs.applyHeaderTheme(this, headerView);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        TextView headerTitle = findViewById(R.id.header_title);
        headerTitle.setText(getString(R.string.daily_checkin));

        txtDate = findViewById(R.id.txt_date);
        txtDate.setText(formatToday());

        FrameLayout btnSave = findViewById(R.id.btn_save_checkin);
        btnSave.setOnClickListener(v -> {
            saveCheckIn();
            Toast.makeText(this, R.string.checkin_saved, Toast.LENGTH_SHORT).show();
            finish();
        });

        seekSleep = findViewById(R.id.seek_sleep);
        seekEnergy = findViewById(R.id.seek_energy);
        seekSoreness = findViewById(R.id.seek_soreness);
        seekStress = findViewById(R.id.seek_stress);
        seekRpe = findViewById(R.id.seek_rpe);

        txtSleepValue = findViewById(R.id.txt_sleep_value);
        txtSleepLabel = findViewById(R.id.txt_sleep_label);

        txtEnergyValue = findViewById(R.id.txt_energy_value);
        txtEnergyLabel = findViewById(R.id.txt_energy_label);

        txtSorenessValue = findViewById(R.id.txt_soreness_value);
        txtSorenessLabel = findViewById(R.id.txt_soreness_label);

        txtStressValue = findViewById(R.id.txt_stress_value);
        txtStressLabel = findViewById(R.id.txt_stress_label);

        txtRpeValue = findViewById(R.id.txt_rpe_value);
        txtRpeLabel = findViewById(R.id.txt_rpe_label);

        txtRecoveryValue = findViewById(R.id.txt_recovery_value);
        txtRecommendation = findViewById(R.id.txt_recommendation);

        loadSavedValues();

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateLabelsAndRecommendation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };

        seekSleep.setOnSeekBarChangeListener(listener);
        seekEnergy.setOnSeekBarChangeListener(listener);
        seekSoreness.setOnSeekBarChangeListener(listener);
        seekStress.setOnSeekBarChangeListener(listener);
        seekRpe.setOnSeekBarChangeListener(listener);

        updateLabelsAndRecommendation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemePrefs.applyHeaderTheme(this, headerView);
    }

    private String formatToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadSavedValues() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        seekSleep.setProgress(prefs.getInt(KEY_SLEEP, 7));
        seekEnergy.setProgress(prefs.getInt(KEY_ENERGY, 7));
        seekSoreness.setProgress(prefs.getInt(KEY_SORENESS, 3));
        seekStress.setProgress(prefs.getInt(KEY_STRESS, 4));
        seekRpe.setProgress(prefs.getInt(KEY_RPE, 5));
    }

    private void updateLabelsAndRecommendation() {
        int sleep = seekSleep.getProgress();
        int energy = seekEnergy.getProgress();
        int soreness = seekSoreness.getProgress();
        int stress = seekStress.getProgress();
        int rpe = seekRpe.getProgress();

        txtSleepValue.setText(String.valueOf(sleep));
        txtSleepLabel.setText(labelSleep(sleep));

        txtEnergyValue.setText(String.valueOf(energy));
        txtEnergyLabel.setText(labelEnergy(energy));

        txtSorenessValue.setText(String.valueOf(soreness));
        txtSorenessLabel.setText(labelSoreness(soreness));

        txtStressValue.setText(String.valueOf(stress));
        txtStressLabel.setText(labelStress(stress));

        txtRpeValue.setText(String.valueOf(rpe));
        txtRpeLabel.setText(labelRpe(rpe));

        float recovery = computeRecovery(sleep, energy, soreness, stress, rpe);
        txtRecoveryValue.setText(formatOneDecimal(recovery));

        txtRecommendation.setText(recommendationText(recovery));
    }

    private float computeRecovery(int sleep, int energy, int soreness, int stress, int rpe) {
        float a = sleep;
        float b = energy;
        float c = 10 - soreness;
        float d = 10 - stress;
        float e = 10 - rpe;
        float result = (a + b + c + d + e) / 5f;
        result = Math.max(0f, Math.min(10f, result));
        return result;
    }

    private String formatOneDecimal(float v) {
        return String.format(Locale.getDefault(), "%.1f", v);
    }

    private String recommendationText(float recovery) {
        if (recovery >= 8f) {
            return getString(R.string.recommendation_excellent);
        } else if (recovery >= 6f) {
            return getString(R.string.recommendation_moderate);
        } else {
            return getString(R.string.recommendation_low);
        }
    }

    private String labelSleep(int v) {
        if (v <= 3) return getString(R.string.sleep_poor);
        if (v >= 8) return getString(R.string.sleep_excellent);
        return getString(R.string.sleep_ok);
    }

    private String labelEnergy(int v) {
        if (v <= 3) return getString(R.string.energy_exhausted);
        if (v >= 8) return getString(R.string.energy_energized);
        return getString(R.string.energy_ok);
    }

    private String labelSoreness(int v) {
        if (v <= 3) return getString(R.string.soreness_none);
        if (v >= 8) return getString(R.string.soreness_very);
        return getString(R.string.soreness_some);
    }

    private String labelStress(int v) {
        if (v <= 3) return getString(R.string.stress_relaxed);
        if (v >= 8) return getString(R.string.stress_stressed);
        return getString(R.string.stress_ok);
    }

    private String labelRpe(int v) {
        if (v <= 3) return getString(R.string.rpe_easy);
        if (v >= 8) return getString(R.string.rpe_max);
        return getString(R.string.rpe_moderate);
    }

    private void saveCheckIn() {
        int sleep = seekSleep.getProgress();
        int energy = seekEnergy.getProgress();
        int soreness = seekSoreness.getProgress();
        int stress = seekStress.getProgress();
        int rpe = seekRpe.getProgress();
        float recovery = computeRecovery(sleep, energy, soreness, stress, rpe);
        long now = System.currentTimeMillis();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_SLEEP, sleep)
                .putInt(KEY_ENERGY, energy)
                .putInt(KEY_SORENESS, soreness)
                .putInt(KEY_STRESS, stress)
                .putInt(KEY_RPE, rpe)
                .putFloat(KEY_RECOVERY, recovery)
                .putLong(KEY_TIMESTAMP, now)
                .apply();

        String existing = prefs.getString(KEY_CHECKIN_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(existing);
            JSONObject item = new JSONObject();
            item.put("timestamp", now);
            item.put("recovery", recovery);
            item.put("sleep", sleep);
            item.put("energy", energy);
            item.put("soreness", soreness);
            item.put("stress", stress);
            item.put("rpe", rpe);
            arr.put(item);
            prefs.edit().putString(KEY_CHECKIN_HISTORY, arr.toString()).apply();
        } catch (JSONException e) {
            JSONArray arr = new JSONArray();
            try {
                JSONObject item = new JSONObject();
                item.put("timestamp", now);
                item.put("recovery", recovery);
                item.put("sleep", sleep);
                item.put("energy", energy);
                item.put("soreness", soreness);
                item.put("stress", stress);
                item.put("rpe", rpe);
                arr.put(item);
                prefs.edit().putString(KEY_CHECKIN_HISTORY, arr.toString()).apply();
            } catch (JSONException ignored) { }
        }
    }
}

