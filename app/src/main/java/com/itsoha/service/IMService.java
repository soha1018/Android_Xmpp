package com.itsoha.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.itsoha.activity.LoginActivity;
import com.itsoha.dbhelper.ContactHelper;
import com.itsoha.dbhelper.SmsHelper;
import com.itsoha.provide.ContactProvide;
import com.itsoha.provide.SmsProvider;
import com.itsoha.utils.PinyinUtils;
import com.itsoha.utils.ThreadUtils;
import com.itsoha.utils.ToastUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 聊天消息的监听和联系人的监听
 */
public class IMService extends Service {
    public static XMPPConnection xmppConnection;
    private static final String TAG = "IMService";
    public static String CURRENT_ACCOUNT;
    private Roster roster;
    private MyRosterListener mRosterListener;
    //存储聊天对象
    private Map<String, Chat> chatMap = new HashMap<>();
    private ChatManager chatManager;
    private Chat mCurChart;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyService();
    }

    /**
     * 使用混合模式创建
     */
    public class MyService extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        //开启线程录入数据
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //查询花名册中的数据
                if (IMService.xmppConnection != null) {

                    roster = IMService.xmppConnection.getRoster();
                    Collection<RosterEntry> entries = roster.getEntries();


                    for (RosterEntry entry : entries) {
                        saveOrUpdateEntry(entry);
                    }
                    mRosterListener = new MyRosterListener();
                    roster.addRosterListener(mRosterListener);
                }

            }
        });

        //创建消息管理者
        if (chatManager == null && IMService.xmppConnection!=null) {
            chatManager = IMService.xmppConnection.getChatManager();
        }
        if (chatManager != null) {
        chatManager.addChatListener(new MyChatListener());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        if (roster != null && mRosterListener != null) {
            roster.removeRosterListener(mRosterListener);
        }
        if (mCurChart != null && mMessageListener != null) {
            mCurChart.removeMessageListener(mMessageListener);
        }
        super.onDestroy();
    }

    /**
     * 更新保存数据
     */
    private void saveOrUpdateEntry(RosterEntry entry) {
        ContentResolver resolver = getContentResolver();
        ContentValues value = new ContentValues();
        String user = entry.getUser();
        String name = entry.getName();
        Log.i(TAG, "saveOrUpdateEntry: "+user);
        if (name == null || name.equals("")) {
            int i = user.indexOf("@");
            if (i > 0) {
                name = user.substring(0, i);
            } else {
                Log.i("service", "saveOrUpdateEntry: " + user);
            }
        }
        value.put(ContactHelper.ContactTable.ACCOUNT, user);
        value.put(ContactHelper.ContactTable.NICKNAME, name);
        value.put(ContactHelper.ContactTable.AVATAR, "8888");
        value.put(ContactHelper.ContactTable.PINYIN, PinyinUtils.getPinyin(name));
        //先查看数据是否更新，再保存数据
        int rows = resolver.update(ContactProvide.URI_CONTACT, value, ContactHelper.ContactTable.ACCOUNT + "=?", new String[]{user});
        if (rows <= 0) {
            resolver.insert(ContactProvide.URI_CONTACT, value);
        }
    }

    class MyRosterListener implements RosterListener {
        @Override
        public void entriesAdded(Collection<String> addresses) {
            if (roster != null) {
                for (String address : addresses) {
                    Log.i(TAG, "entriesAdded: " + address);
                    RosterEntry entry = roster.getEntry(address);
                    saveOrUpdateEntry(entry);
                }
            }

        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            if (roster != null) {
                for (String address : addresses) {
                    Log.i(TAG, "entriesUpdated: " + address);
                    RosterEntry entry = roster.getEntry(address);
                    saveOrUpdateEntry(entry);
                }
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            if (roster != null) {
                for (String address : addresses) {
                    Log.i(TAG, "entriesDeleted: " + address);
                    getContentResolver().delete(ContactProvide.URI_CONTACT, ContactHelper.ContactTable.ACCOUNT + "=?", new String[]{address});
                }
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            Log.i(TAG, "presenceChanged联系人状态:" + presence.getMode().toString());
        }
    }


    private class MyChatListener implements ChatManagerListener {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            //判断chat是否在map里面，提高效率
            String participant = chat.getParticipant();//和我聊天的人

            //因为别人创建和我自己创建的jid不同，需要统一创建
            participant = filterAccount(participant);
            if (!chatMap.containsKey(participant)) {
                chatMap.put(participant, chat);
                chat.addMessageListener(mMessageListener);
            }
        }
    }

    private String filterAccount(String participant) {
        return participant.substring(0,participant.indexOf("@"))+"@"+ LoginActivity.SERVICE_NAME;
    }

    private MyMessageListener mMessageListener = new MyMessageListener();

    private class MyMessageListener implements MessageListener {
        @Override
        public void processMessage(Chat chat, Message message) {
            String body = message.getBody();
            ToastUtils.showToastSafe(IMService.this,body);
            //收到消息，保存消息
            String participant = chat.getParticipant();
            saveMessage(participant,message);
        }
    }


    /**
     * 保存消息
     */
    private void saveMessage(String sessionAccount, Message msg) {
        ContentValues values = new ContentValues();

        sessionAccount = filterAccount(sessionAccount);
        String from = msg.getFrom();
        from = filterAccount(from);
        String to = msg.getTo();
        to = filterAccount(to);

        values.put(SmsHelper.SmsTable.FROM_ACCOUNT, from);
        values.put(SmsHelper.SmsTable.TO_ACCOUNT, to);
        values.put(SmsHelper.SmsTable.BODY, msg.getBody());
        values.put(SmsHelper.SmsTable.STATUS, "offline");
        values.put(SmsHelper.SmsTable.TYPE, msg.getType().name());
        values.put(SmsHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsHelper.SmsTable.SESSION_ACCOUNT, sessionAccount);
        getContentResolver().insert(SmsProvider.SMS_URI, values);
    }

    /**
     * 发送消息
     */
    public void sendMessage(Message msg) {
        try {
            String toAccount = msg.getTo();
            //判断被发送者是否存在于集合中，如果存在就直接获取，反之就创建一个新的存到集合中
            if (chatMap.containsKey(toAccount)) {
                mCurChart = chatMap.get(toAccount);
            } else {
                mCurChart = chatManager.createChat(toAccount, mMessageListener);
                chatMap.put(toAccount, mCurChart);
            }
            mCurChart.sendMessage(msg);
            //保存消息
            saveMessage(toAccount,msg);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

}
