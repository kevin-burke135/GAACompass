package com.example.gaacompass;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AnalyticsStore {

    private static final String PREFS_NAME = "gaacompass_prefs";
    private static final String KEY_FREES_SESSIONS = "frees_sessions";
    private static final String KEY_CHECKIN_HISTORY = "daily_checkins_history";

    private final SharedPreferences prefs;

    public AnalyticsStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public Summary getSummary() {
        List<SessionEntry> sessions = getSessionEntries();
        List<CheckInEntry> checkins = getCheckInEntries();
        Collections.sort(sessions, (a, b) -> Long.compare(b.timestamp, a.timestamp));
        Collections.sort(checkins, (a, b) -> Long.compare(b.timestamp, a.timestamp));

        int totalAttempts = 0;
        int totalScored = 0;
        int bestAcc = 0;
        for (SessionEntry s : sessions) {
            totalAttempts += s.attempts;
            totalScored += s.scored;
            if (s.accuracy > bestAcc) bestAcc = s.accuracy;
        }
        int weightedAvgAcc = totalAttempts == 0 ? 0 : Math.round((100f * totalScored) / totalAttempts);

        return new Summary(
                sessions,
                checkins,
                computeCurrentStreak(checkins),
                computeBestStreak(checkins),
                avgAccuracyWithinDays(sessions, 7),
                avgAccuracyWithinDays(sessions, 30),
                avgAccuracyAll(sessions),
                sumAttemptsForWeekOffset(sessions, 0),
                sumAttemptsForWeekOffset(sessions, 1),
                avgRecoveryForWeekOffset(checkins, 0),
                avgRecoveryForWeekOffset(checkins, 1),
                totalAttempts,
                totalScored,
                weightedAvgAcc,
                bestAcc,
                sessions.isEmpty() ? null : sessions.get(0),
                checkins.isEmpty() ? null : checkins.get(0)
        );
    }

    private List<SessionEntry> getSessionEntries() {
        String raw = prefs.getString(KEY_FREES_SESSIONS, "[]");
        List<SessionEntry> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                out.add(new SessionEntry(
                        obj.optLong("timestamp", 0L),
                        obj.optInt("attempts", 0),
                        obj.optInt("scored", 0),
                        obj.optInt("accuracy", 0)
                ));
            }
        } catch (JSONException ignored) { }
        return out;
    }

    private List<CheckInEntry> getCheckInEntries() {
        String raw = prefs.getString(KEY_CHECKIN_HISTORY, "[]");
        List<CheckInEntry> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj == null) continue;
                out.add(new CheckInEntry(
                        obj.optLong("timestamp", 0L),
                        (float) obj.optDouble("recovery", 0.0)
                ));
            }
        } catch (JSONException ignored) { }
        return out;
    }

    private int avgAccuracyWithinDays(List<SessionEntry> sessions, int days) {
        long now = System.currentTimeMillis();
        long cutoff = now - days * 24L * 60L * 60L * 1000L;
        int count = 0;
        int total = 0;
        for (SessionEntry s : sessions) {
            if (s.timestamp >= cutoff) {
                total += s.accuracy;
                count++;
            }
        }
        return count == 0 ? 0 : Math.round((float) total / count);
    }

    private int avgAccuracyAll(List<SessionEntry> sessions) {
        if (sessions.isEmpty()) return 0;
        int total = 0;
        for (SessionEntry s : sessions) total += s.accuracy;
        return Math.round((float) total / sessions.size());
    }

    private int sumAttemptsForWeekOffset(List<SessionEntry> sessions, int weekOffset) {
        long start = weekStartForOffset(weekOffset);
        long end = start + 7L * 24L * 60L * 60L * 1000L;
        int sum = 0;
        for (SessionEntry s : sessions) {
            if (s.timestamp >= start && s.timestamp < end) sum += s.attempts;
        }
        return sum;
    }

    private float avgRecoveryForWeekOffset(List<CheckInEntry> checkins, int weekOffset) {
        long start = weekStartForOffset(weekOffset);
        long end = start + 7L * 24L * 60L * 60L * 1000L;
        float sum = 0f;
        int count = 0;
        for (CheckInEntry c : checkins) {
            if (c.timestamp >= start && c.timestamp < end) {
                sum += c.recovery;
                count++;
            }
        }
        return count == 0 ? 0f : sum / count;
    }

    private long weekStartForOffset(int weekOffset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        if (diff > 0) diff -= 7;
        cal.add(Calendar.DAY_OF_MONTH, diff - (7 * weekOffset));
        return cal.getTimeInMillis();
    }

    private int computeCurrentStreak(List<CheckInEntry> checkins) {
        Set<String> uniqueDays = new HashSet<>();
        for (CheckInEntry c : checkins) uniqueDays.add(dayKey(c.timestamp));
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        while (true) {
            String key = dayKey(cal.getTimeInMillis());
            if (!uniqueDays.contains(key)) break;
            streak++;
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return streak;
    }

    private int computeBestStreak(List<CheckInEntry> checkins) {
        if (checkins.isEmpty()) return 0;
        List<Long> dates = new ArrayList<>();
        for (CheckInEntry c : checkins) dates.add(startOfDay(c.timestamp));
        Collections.sort(dates);
        int best = 1;
        int current = 1;
        for (int i = 1; i < dates.size(); i++) {
            long prev = dates.get(i - 1);
            long cur = dates.get(i);
            if (cur == prev) continue;
            long diff = cur - prev;
            if (diff == 24L * 60L * 60L * 1000L) {
                current++;
                if (current > best) best = current;
            } else {
                current = 1;
            }
        }
        return best;
    }

    private String dayKey(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.format(Locale.getDefault(), "%04d%02d%02d", year, month, day);
    }

    private long startOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static final class Summary {
        public final List<SessionEntry> sessions;
        public final List<CheckInEntry> checkins;
        public final int currentStreak;
        public final int bestStreak;
        public final int accuracy7Days;
        public final int accuracy30Days;
        public final int accuracyAllTime;
        public final int attemptsThisWeek;
        public final int attemptsLastWeek;
        public final float recoveryThisWeek;
        public final float recoveryLastWeek;
        public final int totalAttempts;
        public final int totalScored;
        public final int weightedAverageAccuracy;
        public final int bestAccuracy;
        public final SessionEntry latestSession;
        public final CheckInEntry latestCheckin;

        Summary(List<SessionEntry> sessions,
                List<CheckInEntry> checkins,
                int currentStreak,
                int bestStreak,
                int accuracy7Days,
                int accuracy30Days,
                int accuracyAllTime,
                int attemptsThisWeek,
                int attemptsLastWeek,
                float recoveryThisWeek,
                float recoveryLastWeek,
                int totalAttempts,
                int totalScored,
                int weightedAverageAccuracy,
                int bestAccuracy,
                SessionEntry latestSession,
                CheckInEntry latestCheckin) {
            this.sessions = sessions;
            this.checkins = checkins;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
            this.accuracy7Days = accuracy7Days;
            this.accuracy30Days = accuracy30Days;
            this.accuracyAllTime = accuracyAllTime;
            this.attemptsThisWeek = attemptsThisWeek;
            this.attemptsLastWeek = attemptsLastWeek;
            this.recoveryThisWeek = recoveryThisWeek;
            this.recoveryLastWeek = recoveryLastWeek;
            this.totalAttempts = totalAttempts;
            this.totalScored = totalScored;
            this.weightedAverageAccuracy = weightedAverageAccuracy;
            this.bestAccuracy = bestAccuracy;
            this.latestSession = latestSession;
            this.latestCheckin = latestCheckin;
        }
    }

    public static final class SessionEntry {
        public final long timestamp;
        public final int attempts;
        public final int scored;
        public final int accuracy;

        SessionEntry(long timestamp, int attempts, int scored, int accuracy) {
            this.timestamp = timestamp;
            this.attempts = attempts;
            this.scored = scored;
            this.accuracy = accuracy;
        }
    }

    public static final class CheckInEntry {
        public final long timestamp;
        public final float recovery;

        CheckInEntry(long timestamp, float recovery) {
            this.timestamp = timestamp;
            this.recovery = recovery;
        }
    }
}

