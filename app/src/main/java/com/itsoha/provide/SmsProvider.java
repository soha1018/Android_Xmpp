package com.itsoha.provide;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.itsoha.dbhelper.SmsHelper;


public class SmsProvider extends ContentProvider {
    //获得当前类路径
    private static final String AUTHORITIES = SmsProvider.class.getCanonicalName();
    private static UriMatcher matcher;
    public static Uri SMS_URI = Uri.parse("content://" + AUTHORITIES + "/sms");
    public static Uri SESSION_URI = Uri.parse("content://" + AUTHORITIES + "/sessions");


    public static final int SMS = 1;

    public static final int SESSION = 23;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITIES, "/sms", SMS);
        matcher.addURI(AUTHORITIES, "/sessions", SESSION);
    }

    private SmsHelper mSmsHelper;


    @Override
    public boolean onCreate() {
        mSmsHelper = new SmsHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = matcher.match(uri);
        Cursor cursor = null;
        SQLiteDatabase db = mSmsHelper.getReadableDatabase();
        switch (match) {
            case SMS:
                cursor = db.query(SmsHelper.T_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SESSION:
                cursor = db.query(SmsHelper.T_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = matcher.match(uri);
        switch (match) {
            case SMS:
                SQLiteDatabase db = mSmsHelper.getReadableDatabase();
                long insert = db.insert(SmsHelper.T_SMS, null, values);
                if (insert > 0) {
                    //把给定的ID附加到Uri的尾部
                    uri = ContentUris.withAppendedId(uri, insert);
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = matcher.match(uri);
        int delete = 0;
        switch (match) {
            case SMS:
                SQLiteDatabase db = mSmsHelper.getReadableDatabase();
                delete = db.delete(SmsHelper.T_SMS, selection, selectionArgs);
                if (delete > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
        }
        return delete;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = matcher.match(uri);
        int update = 0;
        switch (match) {
            case SMS:
                SQLiteDatabase db = mSmsHelper.getReadableDatabase();
                update = db.update(SmsHelper.T_SMS, values, selection, selectionArgs);
                if (update > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
        }
        return update;
    }
}
