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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.p.xunjian.DataType.PublicData;
import com.p.xunjian.R;

public class NetworkActivity extends Activity {

    EditText act_et,psd_et;
    String ip,port;
    public Handler mhandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.network_activity);
        getActionBar().setTitle("服务器地址配置");
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
        Button button = (Button) findViewById(R.id.bt_login);
        act_et = (EditText)findViewById(R.id.tv_act);
        psd_et = (EditText)findViewById(R.id.tv_psd);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = act_et.getText().toString();
                port = psd_et.getText().toString();
                if(ip.length() > 0 && port.length() > 0) {
                    PublicData.getInstance().setIp(ip);
                    PublicData.getInstance().setPort(port);
                    PublicData.getInstance().setHas_save_ip(true);
                    saveLoginInfo();
                    finish();
                }else {
                    new AlertDialog.Builder(NetworkActivity.this)
                            .setMessage("请完整填写服务器地址与端口")
                            .create()
                            .show();
                }
            }
        });
    }
    public void saveLoginInfo(){
        SharedPreferences ps = getSharedPreferences(getResources().getString(R.string.login_preference_name),MODE_PRIVATE);
        SharedPreferences.Editor editor = ps.edit();
        editor.clear();
        editor.putBoolean("SaveInfo",true);
        editor.putString("Ip",ip);
        editor.putString("Port",port);
        editor.commit();
    }
}