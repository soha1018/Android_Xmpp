package com.itsoha.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itsoha.R;
import com.itsoha.activity.ChatActivity;
import com.itsoha.dbhelper.ContactHelper;
import com.itsoha.provide.ContactProvide;
import com.itsoha.service.IMService;
import com.itsoha.utils.PinyinUtils;
import com.itsoha.utils.ThreadUtils;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

public class ContactsFragment extends android.support.v4.app.Fragment {

    private ListView mListView;
    private static final String TAG = "ContactsFragment";
    private LoaderManager loaderManager;
    private ContactAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container,false);
        initView(view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initData();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: 开始");
        //观察者被销毁的时候，fragment销毁并又一次可见的时候，查询数据库显示数据
        setOrUpdateAdaper();
    }

    @Override
    public void onDestroy() {
        unRegisterContentObserver();
//        if (roster != null && mRosterListener!=null) {
//            roster.removeRosterListener(mRosterListener);
//        }
        super.onDestroy();
    }

    private void init() {
        registerContentObserver();
    }


    private void initView(View view) {
        mListView = view.findViewById(R.id.lv_contacts);
    }

    private void initListener() {
        if (mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter != null) {
                        Cursor cursor = adapter.getCursor();
                        cursor.moveToPosition(position);

                        String account = cursor.getString(cursor.getColumnIndex(ContactHelper.ContactTable.ACCOUNT));
                        String nikename = cursor.getString(cursor.getColumnIndex(ContactHelper.ContactTable.NICKNAME));

                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("account", account);
                        intent.putExtra("nickname", nikename);
                        startActivity(intent);

                    }
                }
            });
        }
    }

    private void initData() {
        Log.i(TAG, "initData: 走来了");
        setOrUpdateAdaper();
    }

    private void setOrUpdateAdaper() {
        loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(0, null, new ContactsFragment.ContactsCallBack());
    }

    private class ContactsCallBack implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), ContactProvide.URI_CONTACT, null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (adapter != null) {
                adapter.getCursor().requery();
            }
            if (data.getCount() <= 0) {
                return;
            }
            if (data.moveToFirst()) {
                adapter = new ContactAdapter(getActivity(), data, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                mListView.setAdapter(adapter);
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class ContactAdapter extends CursorAdapter {
        public ContactAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(getActivity(), R.layout.item_contact, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvName = view.findViewById(R.id.nickname);
            TextView tvAccount = view.findViewById(R.id.account);

            int columnIndex = cursor.getColumnIndex(ContactHelper.ContactTable.NICKNAME);
            String name = cursor.getString(columnIndex);
            String account = cursor.getString(cursor.getColumnIndex(ContactHelper.ContactTable.ACCOUNT));
            tvAccount.setText(account);
            tvName.setText(name);
        }
    }



    private MyContactContentOBServer contentOBServer = new MyContactContentOBServer(new Handler());

    /**
     * 注册内容提供者
     */
    private void registerContentObserver() {
        //true  uri=content：//cn.is/**
        if (contentOBServer != null) {
            getActivity().getContentResolver().registerContentObserver(ContactProvide.URI_CONTACT, true, contentOBServer);
        }
    }

    /**
     * 反注册内容提供者
     */
    private void unRegisterContentObserver() {
        if (contentOBServer != null) {
            getActivity().getContentResolver().unregisterContentObserver(contentOBServer);
        }
    }

    /**
     * 自定义内容观察者
     */
    private class MyContactContentOBServer extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContactContentOBServer(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            setOrUpdateAdaper();
        }
    }

}
