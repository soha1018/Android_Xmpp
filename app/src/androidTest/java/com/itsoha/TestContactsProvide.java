package com.itsoha;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.itsoha.dbhelper.ContactHelper;
import com.itsoha.provide.ContactProvide;

public class TestContactsProvide extends AndroidTestCase {
    public void testInsert() {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues value = new ContentValues();
        value.put(ContactHelper.ContactTable.ACCOUNT,"1623848706@qq.com");
        value.put(ContactHelper.ContactTable.NICKNAME,"东爷");
        value.put(ContactHelper.ContactTable.AVATAR,"8888");
        value.put(ContactHelper.ContactTable.PINYIN,"dongye");
        resolver.insert(ContactProvide.URI_CONTACT, value);
    }
    public void testDelete() {
        ContentResolver resolver = getContext().getContentResolver();
        int delete = resolver.delete(ContactProvide.URI_CONTACT, ContactHelper.ContactTable.ACCOUNT + "=?", new String[]{"1623848706@qq.com"});
        Log.i("delete", "testDelete: 删除"+delete);
    }
    public void testUpdate() {
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues value = new ContentValues();
        value.put(ContactHelper.ContactTable.ACCOUNT,"1623848706@qq.com");
        value.put(ContactHelper.ContactTable.NICKNAME,"东哥");
        value.put(ContactHelper.ContactTable.AVATAR,"8888");
        value.put(ContactHelper.ContactTable.PINYIN,"dongge");
        resolver.update(ContactProvide.URI_CONTACT, value, ContactHelper.ContactTable.ACCOUNT + "=?", new String[]{"1623848706@qq.com"});

    }
    public void testQuery() {
        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = resolver.query(ContactProvide.URI_CONTACT, null, null, null, null);
        int count = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            for (int i = 0; i < count; i++) {
                Log.i("testQuery", "testQuery: "+cursor.getString(i)+"      ");
            }
            Log.i("testQuery","      ");
        }
    }
}
