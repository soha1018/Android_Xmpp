package com.itsoha.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itsoha.R;
import com.itsoha.activity.ChatActivity;
import com.itsoha.dbhelper.ContactHelper;
import com.itsoha.dbhelper.SmsHelper;
import com.itsoha.provide.ContactProvide;
import com.itsoha.provide.SmsProvider;
import com.itsoha.service.IMService;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.content.ContentValues.TAG;

public class SessionFragment extends Fragment {

    public static final int ID = 1;
    private ListView mListView;
    private SessionAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mListView = view.findViewById(R.id.listView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        registerObserver();
        setOrUpdateAdapter();
        initListener();
        super.onActivityCreated(savedInstanceState);

    }

    /**
     * 点击跳转到聊天界面
     */
    private void initListener() {
        if (mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapter != null) {
                        Cursor cursor = adapter.getCursor();
                        cursor.moveToPosition(position);

                        String account = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.SESSION_ACCOUNT));
                        String name = getNickNameByAccount(account);
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("account",account);
                        intent.putExtra("nickname",name);
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterObserver();
    }

    private class MySessionCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == ID) {
                return new CursorLoader(getActivity(), SmsProvider.SESSION_URI, null, "from_account=? or to_account=? group by session_account", new String[]{IMService.CURRENT_ACCOUNT, IMService.CURRENT_ACCOUNT}, "time asc");
            } else {
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (adapter != null) {
                adapter.getCursor().requery();
                return;
            }
            Log.i(TAG, "onLoadFinished: ");
            adapter = new SessionAdapter(getActivity(), data, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mListView.setAdapter(adapter);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private class SessionAdapter extends CursorAdapter {
        public SessionAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(context, R.layout.item_session, null);

        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView ivHead = (ImageView) view.findViewById(R.id.head);
            TextView tvBody = (TextView) view.findViewById(R.id.body);
            TextView tvNickName = (TextView) view.findViewById(R.id.nickname);

            String body = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.BODY));
            String acccount = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.SESSION_ACCOUNT));

            String nickName = getNickNameByAccount(acccount);

            // acccount 但是在聊天记录表(sms)里面没有保存别名信息,只有(Contact表里面有)
            tvBody.setText(body);
            tvNickName.setText(nickName);
        }
    }

    public String getNickNameByAccount(String account) {
        String nickName = "";
        Cursor c = getActivity().getContentResolver().query(ContactProvide.URI_CONTACT, null,
                ContactHelper.ContactTable.ACCOUNT + "=?", new String[]{account}, null);
        if (c.getCount() > 0) {// 有数据
            c.moveToFirst();
            nickName = c.getString(c.getColumnIndex(ContactHelper.ContactTable.NICKNAME));
        }
        return nickName;
    }


    private MySessionObserver sessionObserver = new MySessionObserver(new Handler());

    private void registerObserver() {
        if (sessionObserver != null) {
            getActivity().getContentResolver().registerContentObserver(SmsProvider.SMS_URI, true, sessionObserver);
        }
    }

    private void unregisterObserver() {
        if (sessionObserver != null) {
            getActivity().getContentResolver().unregisterContentObserver(sessionObserver);
        }
    }

    private class MySessionObserver extends ContentObserver {


        /**
         * Creates a content observer.
         */
        public MySessionObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.i(TAG, "onChange: ");
            setOrUpdateAdapter();
        }
    }

    private void setOrUpdateAdapter() {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(ID, null, new MySessionCallback());

    }

}
