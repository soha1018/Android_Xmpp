package com.itsoha.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itsoha.R;
import com.itsoha.service.IMService;
import com.itsoha.service.PushService;
import com.itsoha.utils.SpUtils;
import com.itsoha.utils.ThreadUtils;
import com.itsoha.utils.ToastUtils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class LoginActivity extends AppCompatActivity {
    public String HOST = "219.243.137.91";
    public int PORT = 5222;
    private EditText mEtUserName;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnNet;
    private static final String TAG = "LoginActivity";
    public static final String	SERVICE_NAME	= "soha.com";
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initListener();
    }

    private void initListener() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mEtUserName.getText().toString().trim();
                final String password = mEtPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username)) {
                    mEtUserName.setError("用户名不能为空");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mEtPassword.setError("密码不能为空");
                    return;
                }
                //连接并登陆
                connectAndLogin(username, password);


            }
        });

        mBtnNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertNet();
            }
        });
    }

    private class Test implements Thread.UncaughtExceptionHandler {


        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Log.i(TAG, "uncaughtException: " + e.getMessage());
            ToastUtils.showToastSafe(LoginActivity.this, "登录失败");
        }
    }


    /**
     * 弹出网络设置的对话框
     */
    private void alertNet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View view = View.inflate(LoginActivity.this, R.layout.alert_net, null);
        final EditText etIp = view.findViewById(R.id.et_ip);
        final EditText etPort = view.findViewById(R.id.et_port);
        Button btnOk = view.findViewById(R.id.btn_ok);
        final AlertDialog dialog = builder.create();
        dialog.setView(view);
        dialog.show();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = etIp.getText().toString().trim();
                String port = etPort.getText().toString().trim();
                if (TextUtils.isEmpty(ip)) {
                    etIp.setError("请输入IP地址");
                    return;
                }
                if (TextUtils.isEmpty(port)) {
                    etPort.setError("请输入端口");
                    return;
                }
                SpUtils.putValues(LoginActivity.this, "HOST", ip);
                SpUtils.putValues(LoginActivity.this, "PORT", Integer.parseInt(port));
                dialog.dismiss();
            }
        });
    }

    /**
     * 连接并登陆
     */
    private void connectAndLogin(final String username, final String password) {
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setMessage("正在登陆。。");
        dialog.show();

        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {

                try {
                    HOST = (String) SpUtils.getValues(LoginActivity.this, "HOST", HOST);
                    PORT = (int) SpUtils.getValues(LoginActivity.this, "PORT", PORT);
                    ConnectionConfiguration config = new ConnectionConfiguration(HOST, PORT);
                    //额外的配置。用于开发
                    config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);//明文输出
                    config.setDebuggerEnabled(true);//开启调试模式，方便查看发送的内容
                    XMPPConnection connection = new XMPPConnection(config);
                    connection.connect();
                    //登陆
                    connection.login(username, password);
                    //开启获取联系人的服务
                    startService(new Intent(LoginActivity.this, IMService.class));
                    //开启服务接收服务器发送的消息
                    startService(new Intent(LoginActivity.this, PushService.class));

                    //把连接对象给予全局的连接
                    IMService.xmppConnection = connection;
                    //当前账户
                    IMService.CURRENT_ACCOUNT = username + "@" + LoginActivity.SERVICE_NAME;
                    ToastUtils.showToastSafe(LoginActivity.this, "登陆成功");
                    dialog.dismiss();

                    //跳转页面
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } catch (XMPPException e) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    ToastUtils.showToastSafe(getApplicationContext(), "登录失败");
                    e.printStackTrace();
                }

            }
        });


    }


    private void initView() {
        mEtUserName = findViewById(R.id.et_account);
        mEtPassword = findViewById(R.id.et_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mBtnNet = findViewById(R.id.btn_net);
    }

    private class connectAndLogin extends Throwable {
        private String name;
        private String pwd;

        public connectAndLogin(String username, String password) {
            this.name = username;
            this.pwd = password;
        }

    }
}
