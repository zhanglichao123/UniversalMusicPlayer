package com.sunrise.readerdemo;

import android.app.Activity;
import android.preference.PreferenceManager;

public class ShareReferenceSaver
{
    public static void saveData(Activity paramActivity, String paramString1, String paramString2)
    {
        PreferenceManager.getDefaultSharedPreferences(paramActivity).edit().putString(paramString1, paramString2).commit();
    }

    public static String getData(Activity paramActivity, String paramString)
    {
        return PreferenceManager.getDefaultSharedPreferences(paramActivity).getString(paramString, "");
    }
}