package com.itsoha;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.itsoha.dbhelper.SmsHelper;
import com.itsoha.provide.SmsProvider;
import com.itsoha.service.IMService;

public class TestSmsProvider extends AndroidTestCase {
    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(SmsHelper.SmsTable.FROM_ACCOUNT, "billy@itheima.com");
        values.put(SmsHelper.SmsTable.TO_ACCOUNT, "cang@itheima.com");
        values.put(SmsHelper.SmsTable.BODY, "今晚约吗?");
        values.put(SmsHelper.SmsTable.STATUS, "offline");
        values.put(SmsHelper.SmsTable.TYPE, "chat");
        values.put(SmsHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsHelper.SmsTable.SESSION_ACCOUNT, "cang@itheima.com");
        getContext().getContentResolver().insert(SmsProvider.SMS_URI, values);
    }

    public void testDelete() {
        getContext().getContentResolver().delete(SmsProvider.SMS_URI, SmsHelper.SmsTable.FROM_ACCOUNT + "=?",
                new String[] { "billy@itheima.com" });
    }

    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(SmsHelper.SmsTable.BODY, "今晚约吗?我好久没有看到你了.");
        values.put(SmsHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsHelper.SmsTable.SESSION_ACCOUNT, "cang@itheima.com");
        getContext().getContentResolver().update(SmsProvider.SMS_URI, values,
                SmsHelper.SmsTable.FROM_ACCOUNT + "=?", new String[] { "billy@itheima.com" });
    }

    public void testQuery() {
        Cursor c = getContext().getContentResolver().query(SmsProvider.SMS_URI, null, null, null, null);

        // 得到所有的列
        int columnCount = c.getColumnCount();
        while (c.moveToNext()) {
            for (int i = 0; i < columnCount; i++) {
                Log.i("test", "testQuery: "+c.getString(i));
            }
            System.out.println("");
        }
    }

    public void testQuerySession() {
        Cursor c = getContext().getContentResolver().query(SmsProvider.SESSION_URI, null, null, new String[]{"admin@soha.com", "admin@soha.com"}, null);

        // 得到所有的列
        int columnCount = c.getColumnCount();
        while (c.moveToNext()) {
            for (int i = 0; i < columnCount; i++) {
                Log.i("test", "testQuery: "+c.getString(i));
            }
            System.out.println("");
        }
    }
}
