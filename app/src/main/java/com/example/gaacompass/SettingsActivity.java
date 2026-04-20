package com.example.gaacompass;

import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final class ThemeOption {
        final String key;
        final int labelResId;
        final @DrawableRes int swatchDrawableResId;

        ThemeOption(String key, int labelResId, int swatchDrawableResId) {
            this.key = key;
            this.labelResId = labelResId;
            this.swatchDrawableResId = swatchDrawableResId;
        }
    }

    private static final ThemeOption[] THEMES = {
            new ThemeOption(ThemePrefs.THEME_DEFAULT, R.string.theme_default, R.drawable.bg_header_gradient),
            new ThemeOption("galway", R.string.county_galway, R.drawable.bg_header_galway),
            new ThemeOption("leitrim", R.string.county_leitrim, R.drawable.bg_header_leitrim),
            new ThemeOption("mayo", R.string.county_mayo, R.drawable.bg_header_mayo),
            new ThemeOption("roscommon", R.string.county_roscommon, R.drawable.bg_header_roscommon),
            new ThemeOption("sligo", R.string.county_sligo, R.drawable.bg_header_sligo),
            new ThemeOption("carlow", R.string.county_carlow, R.drawable.bg_header_carlow),
            new ThemeOption("dublin", R.string.county_dublin, R.drawable.bg_header_dublin),
            new ThemeOption("kildare", R.string.county_kildare, R.drawable.bg_header_kildare),
            new ThemeOption("kilkenny", R.string.county_kilkenny, R.drawable.bg_header_kilkenny),
            new ThemeOption("laois", R.string.county_laois, R.drawable.bg_header_laois),
            new ThemeOption("longford", R.string.county_longford, R.drawable.bg_header_longford),
            new ThemeOption("louth", R.string.county_louth, R.drawable.bg_header_louth),
            new ThemeOption("meath", R.string.county_meath, R.drawable.bg_header_meath),
            new ThemeOption("offaly", R.string.county_offaly, R.drawable.bg_header_offaly),
            new ThemeOption("westmeath", R.string.county_westmeath, R.drawable.bg_header_westmeath),
            new ThemeOption("wexford", R.string.county_wexford, R.drawable.bg_header_wexford),
            new ThemeOption("wicklow", R.string.county_wicklow, R.drawable.bg_header_wicklow),
            new ThemeOption("clare", R.string.county_clare, R.drawable.bg_header_clare),
            new ThemeOption("cork", R.string.county_cork, R.drawable.bg_header_cork),
            new ThemeOption("kerry", R.string.county_kerry, R.drawable.bg_header_kerry),
            new ThemeOption("limerick", R.string.county_limerick, R.drawable.bg_header_limerick),
            new ThemeOption("tipperary", R.string.county_tipperary, R.drawable.bg_header_tipperary),
            new ThemeOption("waterford", R.string.county_waterford, R.drawable.bg_header_waterford),
            new ThemeOption("antrim", R.string.county_antrim, R.drawable.bg_header_antrim),
            new ThemeOption("armagh", R.string.county_armagh, R.drawable.bg_header_armagh),
            new ThemeOption("cavan", R.string.county_cavan, R.drawable.bg_header_cavan),
            new ThemeOption("derry", R.string.county_derry, R.drawable.bg_header_derry),
            new ThemeOption("donegal", R.string.county_donegal, R.drawable.bg_header_donegal),
            new ThemeOption("down", R.string.county_down, R.drawable.bg_header_down),
            new ThemeOption("fermanagh", R.string.county_fermanagh, R.drawable.bg_header_fermanagh),
            new ThemeOption("monaghan", R.string.county_monaghan, R.drawable.bg_header_monaghan),
            new ThemeOption("tyrone", R.string.county_tyrone, R.drawable.bg_header_tyrone),
    };

    private ThemePrefs themePrefs;
    private LinearLayout themeList;
    private String currentThemeKey;
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar);
        ThemePrefs.applyHeaderTheme(this, toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        themePrefs = new ThemePrefs(this);
        currentThemeKey = themePrefs.getThemeKey();
        themeList = findViewById(R.id.theme_list);

        for (ThemeOption option : THEMES) {
            View row = makeThemeRow(option);
            themeList.addView(row);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemePrefs.applyHeaderTheme(this, toolbar);
    }

    private View makeThemeRow(@NonNull ThemeOption option) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_county_theme, themeList, false);

        View swatch = row.findViewById(R.id.theme_swatch);
        TextView label = row.findViewById(R.id.theme_label);
        ImageView check = row.findViewById(R.id.theme_check);

        swatch.setBackgroundResource(option.swatchDrawableResId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;
            int size = (int) (40 * density);
            float radius = 10f * density;
            swatch.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, size, size, radius);
                }
            });
            swatch.setClipToOutline(true);
        }
        label.setText(option.labelResId);
        label.setTag(option.key);
        check.setVisibility(option.key.equals(currentThemeKey) ? View.VISIBLE : View.GONE);

        row.setTag(option.key);
        row.setOnClickListener(v -> {
            String key = (String) v.getTag();
            themePrefs.setThemeKey(key);
            currentThemeKey = key;
            updateCheckVisibility();
            ThemePrefs.applyHeaderTheme(this, toolbar);
        });

        return row;
    }

    private void updateCheckVisibility() {
        for (int i = 0; i < themeList.getChildCount(); i++) {
            View row = themeList.getChildAt(i);
            String key = (String) row.getTag();
            ImageView check = row.findViewById(R.id.theme_check);
            check.setVisibility(key != null && key.equals(currentThemeKey) ? View.VISIBLE : View.GONE);
        }
    }
}
