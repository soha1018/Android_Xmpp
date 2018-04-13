package com.itsoha.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itsoha.R;
import com.itsoha.dbhelper.SmsHelper;
import com.itsoha.provide.SmsProvider;
import com.itsoha.service.IMService;
import com.itsoha.utils.ThreadUtils;

import org.jivesoftware.smack.packet.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.et_body)
    EditText etBody;
    @InjectView(R.id.btn_send)
    Button btnSend;
    private String mUser;
    private String mName;
    private LoaderManager mLoaderManager;
    private CursorAdapter mAdapter;
    private static final String TAG = "ChatActivity";
    private IMService imService;
    MyServiceConnection	mMyServiceConnection	= new MyServiceConnection();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.inject(this);

        init();
        initData();
        initView();
        initListener();
    }

    private void init() {
        registerObserver();

        //绑定服务
        Intent intent = new Intent(this, IMService.class);
        bindService(intent, mMyServiceConnection, BIND_AUTO_CREATE);//只要绑定存在就自动创建服务
        mUser = getIntent().getStringExtra("account");
        mName = getIntent().getStringExtra("nickname");
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOrUpdateAdapter();
    }

    private void initData() {
        setOrUpdateAdapter();
    }

    private void setOrUpdateAdapter() {
        mLoaderManager = getSupportLoaderManager();
        mLoaderManager.initLoader(0, null, new SmsCallBack());
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        title.setText("与 " + mName + "聊天中");
    }


    private void initListener() {

    }


    @OnClick(R.id.btn_send)
    public void onViewClicked() {
        //发送消息
        final String body = etBody.getText().toString();
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                Message msg = new Message();
                //消息发送者
                msg.setFrom(IMService.CURRENT_ACCOUNT);
                //消息接收者
                msg.setTo(mUser);
                msg.setBody(body);
                msg.setType(Message.Type.chat);

                if (imService != null) {
                    imService.sendMessage(msg);
                }

                ThreadUtils.runInUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etBody.setText("");
                    }
                });
            }
        });
    }


    private class SmsCallBack implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(ChatActivity.this, SmsProvider.SMS_URI, null, "(to_account=? and from_account=?) or (from_account=? and to_account=?)", new String[]{IMService.CURRENT_ACCOUNT,mUser,IMService.CURRENT_ACCOUNT,mUser}, "time asc");

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mAdapter != null) {
                mAdapter.getCursor().requery();
                //设置游标移动到最后
                listView.setSelection(mAdapter.getCursor().getCount() - 1);
                return;
            }
            if (data.moveToFirst()) {
                mAdapter = new ChatAdapter(ChatActivity.this, data, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

                listView.setAdapter(mAdapter);
                //设置游标移动到最后
//                int position = listView.getCount();
                listView.setSelection(mAdapter.getCursor().getCount() - 1);

            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterObserver();
    }

    private MySmsContentObserver contentObserver = new MySmsContentObserver(new Handler());

    private void registerObserver() {
        if (contentObserver != null) {
            getContentResolver().registerContentObserver(SmsProvider.SMS_URI, true, contentObserver);
        }
    }

    private void unregisterObserver() {
        if (contentObserver != null)
            getContentResolver().unregisterContentObserver(contentObserver);
    }
    private class MySmsContentObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MySmsContentObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.i(TAG, "onChange: ");
            setOrUpdateAdapter();
        }
    }

    /**
     * 聊天界面的适配器
     */
    private class ChatAdapter extends CursorAdapter {
        public static final int RECEIVE = 0;
        public static final int SEND = 1;
        private Context context;
        private Cursor cursor;

        ChatAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            this.context = context;
            this.cursor = c;
        }

        @Override
        public int getItemViewType(int position) {
            //判断是接收者还是发送者
            cursor.moveToPosition(position);
            String formAccount = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.FROM_ACCOUNT));
            if (!formAccount.equals(IMService.CURRENT_ACCOUNT)) {
                return RECEIVE;
            } else {
                return SEND;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (getItemViewType(position) == RECEIVE) {
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.item_chat_receive, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

                    holder.ivHead = convertView.findViewById(R.id.head);
                    holder.tvBody = convertView.findViewById(R.id.content);
                    holder.tvTime = convertView.findViewById(R.id.time);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            } else {
                //发送者
                if (convertView == null) {
                    convertView = View.inflate(context, R.layout.item_chat_send, null);
                    holder = new ViewHolder();
                    convertView.setTag(holder);

                    holder.ivHead = convertView.findViewById(R.id.head);
                    holder.tvBody = convertView.findViewById(R.id.content);
                    holder.tvTime = convertView.findViewById(R.id.time);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
            }

            //游标移动到此处
            cursor.moveToPosition(position);
            String body = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.BODY));
            String time = cursor.getString(cursor.getColumnIndex(SmsHelper.SmsTable.TIME));
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(time)));
            holder.tvBody.setText(body);
            holder.tvTime.setText(time);
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            //两种类型，一个接收，一个发送
            return super.getViewTypeCount() + 1;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return null;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

        }

        class ViewHolder {
            TextView tvBody;
            TextView tvTime;
            ImageView ivHead;
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected: 服务连接成功");
            IMService.MyService myService = (IMService.MyService) service;
            imService = myService.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceConnected: 服务连接失败");
        }
    }
}
