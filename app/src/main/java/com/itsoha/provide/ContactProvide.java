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

import com.itsoha.dbhelper.ContactHelper;

public class ContactProvide extends ContentProvider {
    //得到一个类的完整类路径
    public static String AUTHORITIES = ContactProvide.class.getCanonicalName();
    //地址匹配对象
    private static UriMatcher matcher;
    //对应联系人列表的URI常量
    public static Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");

    public static final int CONTACT = 3;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITIES, "/contact", CONTACT);
    }

    private ContactHelper mHelper;


    @Override
    public boolean onCreate() {
        mHelper = new ContactHelper(getContext());
        if (mHelper != null) {
            return true;
        }
        return false;
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
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                long rows = db.insert(ContactHelper.T_CONTACT, null, values);
                if (rows != -1) {
                    Log.i("insert", "插入了第几行：" + rows);
                    //把给定的ID附加到Uri的尾部
                    uri = ContentUris.withAppendedId(uri, rows);
                    //为null所有的都可以收到
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = matcher.match(uri);
        int rows = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                rows = db.delete(ContactHelper.T_CONTACT, selection, selectionArgs);
                //受影响的行数
                if (rows > 0) {
                    Log.i("delete", "delete: 删除了几行：" + rows);
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);

                }
                break;
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = matcher.match(uri);
        int rows = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                rows = db.update(ContactHelper.T_CONTACT, values, selection, selectionArgs);
                //受影响的行数
                if (rows > 0) {
                    Log.i("update", "update: 更新了几行：" + rows);
                    getContext().getContentResolver().notifyChange(URI_CONTACT, null);
                }

                break;
        }
        return rows;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = matcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.query(ContactHelper.T_CONTACT, projection, selection, selectionArgs, null, null, sortOrder);
                int columnCount = cursor.getColumnCount();
                while (cursor.moveToNext()) {
                    for (int i = 0; i < columnCount; i++) {
                        Log.i("test", "testQueryContact: "+cursor.getString(i));
                    }
                    System.out.println("");
                }
                break;
        }
        return cursor;
    }
}
