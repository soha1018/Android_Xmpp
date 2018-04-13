package com.itsoha.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ContactHelper extends SQLiteOpenHelper {

    public static final String T_CONTACT = "t_contact";

    public ContactHelper(Context context) {
        super(context, "Contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + T_CONTACT + "(_id integer PRIMARY KEY AUTOINCREMENT,"+ContactTable.ACCOUNT+" TEXT,"+ContactTable.NICKNAME+" TEXT,"+ContactTable.AVATAR+" TEXT,"+ContactTable.PINYIN+" TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static class ContactTable implements BaseColumns {
        public static final String ACCOUNT = "account";//账号
        public static final String NICKNAME = "nickname";//昵称
        public static final String AVATAR = "avatar";//头像
        public static final String PINYIN = "pinyin";//账号拼音
    }
}
