package com.air.airspeed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

public class ForgetPwdActivity extends AppCompatActivity {

    ImageView backarrow;
    TextView backtitle;
    EditText account, newpass;
    Button modpass;
    String activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        Intent intent0 = getIntent();
        activity = intent0.getStringExtra("activity");

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        backtitle = (TextView) findViewById(R.id.backtitle);
        account = (EditText) findViewById(R.id.account);
        newpass = (EditText) findViewById(R.id.newpass);
        modpass = (Button) findViewById(R.id.modpass);
        if (activity.equals("Login")) {
            backtitle.setText("找回密码");
        }else if (activity.equals("UserInfo")){
            backtitle.setText("修改密码");
        }
    }

    public void setClick() {
        backarrow.setOnClickListener(new ForgetPwdClick());
        modpass.setOnClickListener(new ForgetPwdClick());
    }

    class ForgetPwdClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    if (activity.equals("Login")) {
                        Intent intent = new Intent(ForgetPwdActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }else if (activity.equals("UserInfo")) {
                        Intent intent1 = new Intent(ForgetPwdActivity.this,UserInfo.class);
                        startActivity(intent1);
                    }
                    break;
                case R.id.modpass:
                    modifypassword();
                    break;
                default:
                        break;

            }
        }
    }

    private void modifypassword() {
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("UserName", account.getText().toString());
        mFormBodyBuild.add("Password", newpass.getText().toString());
        mFormBodyBuild.add("MemberDueDate","");
        mFormBodyBuild.add("Credits","0");

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/ModifyPwdServlet")
                .post(mFormBody)
                .build();

        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {

                try{
                    String responseData = response.body().string();
                    JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                    String result = jsonObject.getString("Result");
                    if (result.equals("success")) {
                        //save info to sharedpreference
                        User user = new User();
                        user.setUserName(jsonObject.getString("UserName"));
                        user.setPassWord(jsonObject.getString("Password"));
                        user.setDueDate(jsonObject.getString("MemberDueDate"));
                        user.setCredits(jsonObject.getInt("Credits"));
                        user.setVersion(jsonObject.getString("Version"));
                        saveInfo(user);
                        Intent intent = new Intent(ForgetPwdActivity.this, LoginSuccessActivity.class);
                        startActivity(intent);
                    }else {
                        ForgetPwdActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPwdActivity.this);
                                builder.setMessage("服务器修改不成功，请稍后再试");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface arg0,int arg1) {
                                        arg0.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }
                } catch (JSONException e) {

                    ForgetPwdActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPwdActivity.this);
                            builder.setMessage("服务器返回异常，请重试");
                            builder.setTitle("提示");
                            builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface arg0,int arg1) {
                                    arg0.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {

                ForgetPwdActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ForgetPwdActivity.this);
                        builder.setMessage("网络无连接，请稍后再试");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface arg0,int arg1) {

                                arg0.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                });
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void saveInfo(User user) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = getSharedPreferences("UserInfo",MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean("isSaved",true);
        editor.putString("UserName", user.getUserName());
        editor.putString("MemberDueDate", user.getDueDate());
        editor.putString("Credits", String.valueOf(user.getCredits()));
        editor.putString("Version", user.getVersion());
        editor.putBoolean("rememberpwd",true);
        editor.putString("Password", user.getPassWord());
        editor.commit();


        NetApp.mInstance.setUser(user);
    }

}


