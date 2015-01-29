package com.p.xunjian.Activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.p.xunjian.DataType.PublicData;
import com.p.xunjian.R;
import com.p.xunjian.Util.NetWorkService;

public class LoginActivity extends Activity {
    ProgressDialog pro_dialog;
    public final static int LOGINACTIVITY_LABEL = 1;
    public final static int LOGIN_SUCCESS = 2;
    public final static int SERVER_ERR = 3;
    public final static int LOGIN_FAIL_NO_ACCOUNT = 4;
    public final static int LOGIN_FAIL_NO_ACCOUNT_AND_PSW = 5;
    public final static int LOGIN_FAIL_ALREADY_LOGIN = 6;
    EditText act_et,psd_et;
    public Handler mhandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_activity);
        getActionBar().setTitle("巡检工具");
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.backcolor_norock));
        final Button button = (Button) findViewById(R.id.bt_login);
        getSavedInfo();
        act_et = (EditText)findViewById(R.id.tv_act);
        psd_et = (EditText)findViewById(R.id.tv_psd);
        mhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == LOGIN_SUCCESS){
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    pro_dialog.dismiss();
                    finish();
                }else if(msg.what == LOGIN_FAIL_NO_ACCOUNT || msg.what == LOGIN_FAIL_ALREADY_LOGIN
                        || msg.what == LOGIN_FAIL_NO_ACCOUNT_AND_PSW || msg.what == SERVER_ERR){
                    String errmsg = null;
                    switch (msg.what){
                        case LOGIN_FAIL_ALREADY_LOGIN:
                            errmsg = "账户已经登录，请勿重复登录！";
                                    break;
                        case LOGIN_FAIL_NO_ACCOUNT:
                            errmsg = "无此账户！";
                            break;
                        case LOGIN_FAIL_NO_ACCOUNT_AND_PSW:
                            errmsg = "帐号或密码错误！";
                            break;
                        case SERVER_ERR:
                            errmsg = "服务器返回数据错误！";
                            break;
                    }
                    pro_dialog.dismiss();
                    AlertDialog.Builder builder  = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("登录失败");
                    builder.setMessage(errmsg);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        };
        PublicData.getInstance().getHandlerHashMap().put(getClass().getName(),mhandler);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                //startActivity(intent);
                //pro_dialog.dismiss();
                if(PublicData.getInstance().isHas_save_ip()){
                    Log.d("LoginActivity", "点击登录按钮");
                    pro_dialog = new ProgressDialog(LoginActivity.this);
                    pro_dialog.setMessage("正在登录，请稍候");
                    pro_dialog.setIndeterminate(false);
                    pro_dialog.setCancelable(false);
                    pro_dialog.show();
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this,NetWorkService.class);
                    intent.putExtra("ActivityName", LoginActivity.this.getClass().getName());
                    intent.putExtra("ReuqestType","login");
                    String user,psw;
                    user = act_et.getText().toString();
                    psw = psd_et.getText().toString();
                    if(user.length() > 0 && psw.length() > 0){
                        PublicData.getInstance().setUser(user);
                        PublicData.getInstance().setPsw(psw);
                        startService(intent);
                    }else{
                        pro_dialog.dismiss();
                        Toast.makeText(LoginActivity.this,"请填写帐号密码",Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(LoginActivity.this,"请填写服务器地址",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void onServerChange(View v){
        Log.d(getClass().getName(),"点击textview");
        Intent intent = new Intent(LoginActivity.this,NetworkActivity.class);
        startActivity(intent);
    }
    public void getSavedInfo(){
        SharedPreferences preferences = getSharedPreferences(getResources().getString(R.string.login_preference_name),MODE_PRIVATE);
        if(preferences.getBoolean("SaveInfo", false)){
            PublicData.getInstance().setIp(preferences.getString("Ip",""));
            PublicData.getInstance().setPort(preferences.getString("Port",""));
            PublicData.getInstance().setHas_save_ip(true);
        }else PublicData.getInstance().setHas_save_ip(false);
    }
}