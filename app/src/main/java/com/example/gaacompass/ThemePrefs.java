package com.example.gaacompass;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.DrawableRes;

public final class ThemePrefs {

    private static final String PREFS_NAME = "gaacompass_prefs";
    private static final String KEY_HEADER_THEME = "header_theme";

    public static final String THEME_DEFAULT = "default";

    private final SharedPreferences prefs;

    public ThemePrefs(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getThemeKey() {
        return prefs.getString(KEY_HEADER_THEME, THEME_DEFAULT);
    }

    public void setThemeKey(String key) {
        prefs.edit().putString(KEY_HEADER_THEME, key).apply();
    }

    @DrawableRes
    public static int getHeaderDrawableForTheme(String themeKey) {
        if (themeKey == null) themeKey = THEME_DEFAULT;
        switch (themeKey) {
            case "antrim":    return R.drawable.bg_header_antrim;
            case "armagh":    return R.drawable.bg_header_armagh;
            case "carlow":    return R.drawable.bg_header_carlow;
            case "cavan":     return R.drawable.bg_header_cavan;
            case "clare":    return R.drawable.bg_header_clare;
            case "cork":     return R.drawable.bg_header_cork;
            case "derry":    return R.drawable.bg_header_derry;
            case "donegal":  return R.drawable.bg_header_donegal;
            case "down":     return R.drawable.bg_header_down;
            case "dublin":   return R.drawable.bg_header_dublin;
            case "fermanagh": return R.drawable.bg_header_fermanagh;
            case "galway":   return R.drawable.bg_header_galway;
            case "kerry":    return R.drawable.bg_header_kerry;
            case "kildare":  return R.drawable.bg_header_kildare;
            case "kilkenny": return R.drawable.bg_header_kilkenny;
            case "laois":    return R.drawable.bg_header_laois;
            case "leitrim":  return R.drawable.bg_header_leitrim;
            case "limerick": return R.drawable.bg_header_limerick;
            case "longford": return R.drawable.bg_header_longford;
            case "louth":    return R.drawable.bg_header_louth;
            case "mayo":     return R.drawable.bg_header_mayo;
            case "meath":    return R.drawable.bg_header_meath;
            case "monaghan": return R.drawable.bg_header_monaghan;
            case "offaly":   return R.drawable.bg_header_offaly;
            case "roscommon": return R.drawable.bg_header_roscommon;
            case "sligo":    return R.drawable.bg_header_sligo;
            case "tipperary": return R.drawable.bg_header_tipperary;
            case "tyrone":   return R.drawable.bg_header_tyrone;
            case "waterford": return R.drawable.bg_header_waterford;
            case "westmeath": return R.drawable.bg_header_westmeath;
            case "wexford":  return R.drawable.bg_header_wexford;
            case "wicklow":  return R.drawable.bg_header_wicklow;
            default:         return R.drawable.bg_header_gradient;
        }
    }
}
