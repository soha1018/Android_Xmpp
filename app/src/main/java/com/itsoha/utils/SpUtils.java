package com.itsoha.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SpUtils {
    public static void putValues(Context context, String tag, Object values) {
        SharedPreferences sp = context.getSharedPreferences("MySp", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        if (values instanceof String) {
            edit.putString(tag, (String) values);
        }
        if (values instanceof Boolean) {
            edit.putBoolean(tag, (Boolean) values);
        }
        if (values instanceof Float) {
            edit.putFloat(tag, (Float) values);
        }
        if (values instanceof Integer) {
            edit.putInt(tag, (Integer) values);
        }
        if (values instanceof Long) {
            edit.putLong(tag, (Long) values);
        }
        if (values instanceof Set) {
            edit.putStringSet(tag, (Set<String>) values);
        }
        edit.apply();
    }

    public static Object getValues(Context context, String tag, Object def) {
        SharedPreferences sp = context.getSharedPreferences("MySp", Context.MODE_PRIVATE);
        if (def instanceof String) {
            return sp.getString(tag, (String) def);
        }
        if (def instanceof Boolean) {
            return sp.getBoolean(tag, (Boolean) def);
        }
        if (def instanceof Float) {
            return sp.getFloat(tag, (Float) def);
        }
        if (def instanceof Long) {
            return sp.getLong(tag, (Long) def);
        }
        if (def instanceof Integer) {
            return sp.getInt(tag, (Integer) def);
        }
        if (def instanceof Set) {
            return sp.getStringSet(tag, (Set<String>) def);
        }
        return null;
    }
}
