package com.example.gaacompass;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyPlannerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "gaacompass_prefs";
    private static final String KEY_LAST_WEEK_START = "weekly_plan_last_week_start";

    private static final int PLAN_REST = 0;
    private static final int PLAN_RECOVER = 1;
    private static final int PLAN_GYM = 2;
    private static final int PLAN_TRAINING = 3;
    private static final int PLAN_MATCH = 4;
    private static final double WEIGHT_REST = 0.0;
    private static final double WEIGHT_RECOVER = 0.5;
    private static final double WEIGHT_GYM = 1.5;
    private static final double WEIGHT_TRAINING = 2.0;
    private static final double WEIGHT_MATCH = 3.0;

    private long weekStartMillis;

    private TextView txtWeekRange;
    private TextView txtWeeklySummary;
    private TextView txtWeeklyPrompt;
    private View headerView;
    private View[] dayViews = new View[7];

    private TextView[] dayDateViews = new TextView[7];

    private FrameLayout[][] optionViews = new FrameLayout[7][5];
    private int[] selections = defaultSelections();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_planner);
        headerView = findViewById(R.id.header);
        ThemePrefs.applyHeaderTheme(this, headerView);

        ImageButton btnPrev = findViewById(R.id.btn_prev_week);
        ImageButton btnNext = findViewById(R.id.btn_next_week);
        txtWeekRange = findViewById(R.id.txt_week_range);
        txtWeeklySummary = findViewById(R.id.txt_weekly_summary);
        txtWeeklyPrompt = findViewById(R.id.txt_weekly_prompt);

        dayViews[0] = findViewById(R.id.day_0);
        dayViews[1] = findViewById(R.id.day_1);
        dayViews[2] = findViewById(R.id.day_2);
        dayViews[3] = findViewById(R.id.day_3);
        dayViews[4] = findViewById(R.id.day_4);
        dayViews[5] = findViewById(R.id.day_5);
        dayViews[6] = findViewById(R.id.day_6);

        for (int i = 0; i < 7; i++) {
            View day = dayViews[i];
            TextView dayName = day.findViewById(R.id.txt_day_name);
            dayName.setText(dayNameForIndex(i));
            dayDateViews[i] = day.findViewById(R.id.txt_day_date);

            optionViews[i][PLAN_REST] = day.findViewById(R.id.opt_rest);
            optionViews[i][PLAN_RECOVER] = day.findViewById(R.id.opt_recover);
            optionViews[i][PLAN_GYM] = day.findViewById(R.id.opt_gym);
            optionViews[i][PLAN_TRAINING] = day.findViewById(R.id.opt_training);
            optionViews[i][PLAN_MATCH] = day.findViewById(R.id.opt_match);
        }

        long savedLastWeekStart = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getLong(KEY_LAST_WEEK_START, 0L);
        if (savedLastWeekStart > 0) {
            weekStartMillis = savedLastWeekStart;
        } else {
            weekStartMillis = startOfWeekMillis(System.currentTimeMillis());
        }

        applyWeek(loadSelections(weekStartMillis));

        btnPrev.setOnClickListener(v -> {
            weekStartMillis -= 7L * 24L * 60L * 60L * 1000L;
            applyWeek(loadSelections(weekStartMillis));
        });

        btnNext.setOnClickListener(v -> {
            weekStartMillis += 7L * 24L * 60L * 60L * 1000L;
            applyWeek(loadSelections(weekStartMillis));
        });

        findViewById(R.id.btn_save_plan).setOnClickListener(v -> onSavePlanClicked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemePrefs.applyHeaderTheme(this, headerView);
    }

    private void applyWeek(int[] newSelections) {
        if (newSelections != null && newSelections.length == 7) {
            selections = newSelections;
        }

        txtWeekRange.setText(formatWeekRange(weekStartMillis));
        updateDayDates(weekStartMillis);

        for (int day = 0; day < 7; day++) {
            setOptionSelected(day, PLAN_REST, selections[day] == PLAN_REST);
            setOptionSelected(day, PLAN_RECOVER, selections[day] == PLAN_RECOVER);
            setOptionSelected(day, PLAN_GYM, selections[day] == PLAN_GYM);
            setOptionSelected(day, PLAN_TRAINING, selections[day] == PLAN_TRAINING);
            setOptionSelected(day, PLAN_MATCH, selections[day] == PLAN_MATCH);
        }

        updateWeeklySummaryAndPrompt();
    }

    private void setOptionSelected(int day, int optionIndex, boolean selected) {
        FrameLayout view = optionViews[day][optionIndex];
        if (view == null) return;
        view.setBackgroundResource(selected ? R.drawable.bg_plan_option_selected : R.drawable.bg_plan_option_unselected);
        view.setOnClickListener(v -> {
            selections[day] = optionIndex;
            applyWeek(selections);
        });
    }

    private int[] loadSelections(long weekStart) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String stored = prefs.getString(weeklyKey(weekStart), null);
        if (stored == null || stored.isEmpty()) return defaultSelections();

        String[] parts = stored.split(",");
        if (parts.length != 7) return defaultSelections();

        int[] result = new int[7];
        try {
            for (int i = 0; i < 7; i++) {
                result[i] = Integer.parseInt(parts[i]);
            }
        } catch (Exception e) {
            return defaultSelections();
        }
        return result;
    }

    private int[] defaultSelections() {
        return new int[]{PLAN_REST, PLAN_GYM, PLAN_TRAINING, PLAN_RECOVER, PLAN_GYM, PLAN_MATCH, PLAN_RECOVER};
    }

    private void saveSelections(long weekStart, int[] data) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            sb.append(data[i]);
        }
        prefs.edit()
                .putString(weeklyKey(weekStart), sb.toString())
                .putLong(KEY_LAST_WEEK_START, weekStart)
                .apply();
    }

    private void onSavePlanClicked() {
        int matches = countType(PLAN_MATCH);
        if (matches >= 2) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.weekly_two_match_warning_title)
                    .setMessage(R.string.weekly_two_match_warning_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.save_anyway, (dialog, which) -> savePlanNow())
                    .show();
            return;
        }
        savePlanNow();
    }

    private void savePlanNow() {
        saveSelections(weekStartMillis, selections);
        Toast.makeText(this, R.string.plan_saved, Toast.LENGTH_SHORT).show();
    }

    private String weeklyKey(long weekStart) {
        return "weekly_plan_week_" + weekStart;
    }

    private String dayNameForIndex(int index) {
        switch (index) {
            case 0:
                return getString(R.string.weekday_monday);
            case 1:
                return getString(R.string.weekday_tuesday);
            case 2:
                return getString(R.string.weekday_wednesday);
            case 3:
                return getString(R.string.weekday_thursday);
            case 4:
                return getString(R.string.weekday_friday);
            case 5:
                return getString(R.string.weekday_saturday);
            default:
                return getString(R.string.weekday_sunday);
        }
    }

    private long startOfWeekMillis(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        if (diff > 0) diff -= 7;
        cal.add(Calendar.DAY_OF_MONTH, diff);
        return cal.getTimeInMillis();
    }

    private String formatWeekRange(long startMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startMillis);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(startMillis);
        end.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.getDefault());
        return fmt.format(cal.getTime()) + " - " + fmt.format(end.getTime());
    }

    private void updateDayDates(long startMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startMillis);
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            if (dayDateViews[i] != null) {
                dayDateViews[i].setText(fmt.format(cal.getTime()));
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateWeeklySummaryAndPrompt() {
        int rest = countType(PLAN_REST);
        int recover = countType(PLAN_RECOVER);
        int gym = countType(PLAN_GYM);
        int training = countType(PLAN_TRAINING);
        int match = countType(PLAN_MATCH);
        double loadScore = calculateWeeklyLoadScore();

        String summary = getString(
                R.string.weekly_summary_format,
                rest,
                recover,
                gym,
                training,
                match,
                String.format(Locale.getDefault(), "%.1f", loadScore)
        );
        txtWeeklySummary.setText(summary);

        String baseFeedback;
        if (loadScore <= 5.0) {
            baseFeedback = getString(R.string.weekly_load_feedback_undertraining);
        } else if (loadScore <= 10.0) {
            baseFeedback = getString(R.string.weekly_load_feedback_optimal);
        } else if (loadScore <= 13.0) {
            baseFeedback = getString(R.string.weekly_load_feedback_high_caution);
        } else {
            baseFeedback = getString(R.string.weekly_load_feedback_overload);
        }

        List<String> warnings = new ArrayList<>();
        warnings.addAll(buildRedFlagWarnings(match, rest, recover));
        warnings.addAll(buildAmberFlagWarnings(match));

        if (!warnings.isEmpty()) {
            StringBuilder promptBuilder = new StringBuilder(baseFeedback);
            for (String warning : warnings) {
                promptBuilder.append("\n• ").append(warning);
            }
            txtWeeklyPrompt.setText(promptBuilder.toString());
            return;
        }

        txtWeeklyPrompt.setText(baseFeedback);
    }

    private int countType(int type) {
        int count = 0;
        for (int value : selections) {
            if (value == type) count++;
        }
        return count;
    }

    private double calculateWeeklyLoadScore() {
        double total = 0.0;
        for (int selection : selections) {
            total += weightForSelection(selection);
        }
        return total;
    }

    private double weightForSelection(int selection) {
        switch (selection) {
            case PLAN_RECOVER:
                return WEIGHT_RECOVER;
            case PLAN_GYM:
                return WEIGHT_GYM;
            case PLAN_TRAINING:
                return WEIGHT_TRAINING;
            case PLAN_MATCH:
                return WEIGHT_MATCH;
            case PLAN_REST:
            default:
                return WEIGHT_REST;
        }
    }

    private List<String> buildRedFlagWarnings(int match, int rest, int recover) {
        List<String> warnings = new ArrayList<>();
        int totalRecoveryDays = rest + recover;

        if (match > 1) {
            warnings.add(getString(R.string.weekly_red_flag_more_than_one_match));
        }
        if (totalRecoveryDays < 2) {
            warnings.add(getString(R.string.weekly_red_flag_low_recovery_days));
        }
        if (hasBackToBackMatchOrMatchThenPitch()) {
            warnings.add(getString(R.string.weekly_red_flag_back_to_back_high_intensity));
        }
        if (rest == 0 && recover == 0) {
            warnings.add(getString(R.string.weekly_red_flag_no_rest_or_recovery));
        }

        return warnings;
    }

    private List<String> buildAmberFlagWarnings(int match) {
        List<String> warnings = new ArrayList<>();
        if (hasMoreThanThreeConsecutiveTrainingDays()) {
            warnings.add(getString(R.string.weekly_amber_flag_consecutive_training));
        }
        if (match == 2) {
            warnings.add(getString(R.string.weekly_amber_flag_two_matches));
        }
        return warnings;
    }

    private boolean hasBackToBackMatchOrMatchThenPitch() {
        for (int i = 0; i < selections.length - 1; i++) {
            int current = selections[i];
            int next = selections[i + 1];
            if (current == PLAN_MATCH && next == PLAN_MATCH) return true;
            if (current == PLAN_MATCH && next == PLAN_TRAINING) return true;
        }
        return false;
    }

    private boolean hasMoreThanThreeConsecutiveTrainingDays() {
        int consecutive = 0;
        for (int selection : selections) {
            if (selection == PLAN_GYM || selection == PLAN_TRAINING || selection == PLAN_MATCH) {
                consecutive++;
                if (consecutive > 3) {
                    return true;
                }
            } else {
                consecutive = 0;
            }
        }
        return false;
    }
}

