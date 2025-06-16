package com.air.airspeed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import static java.lang.Thread.sleep;

public class LoginActivity extends AppCompatActivity {

    EditText account, password;
    TextView shortmsg, forgetpwd, registerin;
    Button login;
    CheckBox rememberpwd, usedisp;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean remember;
    private String  username,pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        setClick();

        pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        editor = pref.edit();
        username = pref.getString("UserName","");
        remember = pref.getBoolean("rememberpwd",false);
        account.setText(username);
        if (remember == true)
        {
            pwd = pref.getString("Password","");
            password.setText(pwd);
        }
    }

    public void initView() {
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        shortmsg = (TextView) findViewById(R.id.shortmsg);
        forgetpwd = (TextView) findViewById(R.id.forgetpwd);
        registerin = (TextView) findViewById(R.id.registerin);
        login = (Button) findViewById(R.id.login);
        rememberpwd = (CheckBox) findViewById(R.id.rememberpwd);
        rememberpwd.setChecked(true);
        usedisp = (CheckBox) findViewById(R.id.usedisp);
    }

    public void setClick() {
        shortmsg.setOnClickListener(new LoginClick());
        forgetpwd.setOnClickListener(new LoginClick());
        login.setOnClickListener(new LoginClick());
        registerin.setOnClickListener(new LoginClick());
        rememberpwd.setOnClickListener(new LoginClick());
        usedisp.setOnClickListener(new LoginClick());
    }

    class LoginClick implements View.OnClickListener  {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shortmsg:
                    break;
                case R.id.forgetpwd:
                    Intent intent1 = new Intent(LoginActivity.this, ForgetPwdActivity.class);
                    intent1.putExtra("activity","Login");
                    startActivity(intent1);
                    break;
                case R.id.login:
                    login();
                    break;
                case R.id.registerin:
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    break;
                case R.id.rememberpwd:
                    if (rememberpwd.isChecked() != remember){
                        editor.putBoolean("rememberpwd", rememberpwd.isChecked());
                        editor.commit();
                    }
                    break;
                case R.id.usedisp:
                    if (usedisp.isChecked() != true) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setMessage("不同意用户协议，您将无法登录");
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
                    break;
                default:
                        break;

            }
        }
    }

    private void login(){
        if (usedisp.isChecked() != true){
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("请同意用户协议");
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

        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("UserName", account.getText().toString());
        mFormBodyBuild.add("Password", password.getText().toString());

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/LoginServlet")
                .post(mFormBody)
                .build();
        Log.d("dopostlogin", "have built login request");

        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d("dopostrequest", "already send and receive response");
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
                        user.setHeadPic(jsonObject.getString("HeadPic"));
                        saveInfo(user);
                        Intent intent = new Intent(LoginActivity.this, LoginSuccessActivity.class);
                        startActivity(intent);
                    }else if(result.equals("outofdate")) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage("账号已过期，请购买会员");
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
                        try{
                            sleep(2000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        //save info to sharedpreference
                        User user = new User();
                        user.setUserName(jsonObject.getString("UserName"));
                        user.setPassWord(jsonObject.getString("Password"));
                        user.setDueDate(jsonObject.getString("MemberDueDate"));
                        user.setCredits(jsonObject.getInt("Credits"));
                        user.setVersion(jsonObject.getString("Version"));
                        user.setHeadPic(jsonObject.getString("HeadPic"));
                        saveInfo(user);
                        Intent intent = new Intent(LoginActivity.this, MemberCenterActivity.class);
                        startActivity(intent);
                    }
                    else {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage("账号或密码错误");
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
                    Log.e("TAG",e.getMessage(),e);
                    Log.d("JSonException",Log.getStackTraceString(e));
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                Log.d("fail response", Log.getStackTraceString(e));
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
        pref = getSharedPreferences("UserInfo",MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean("isSaved",true);
        editor.putString("UserName", user.getUserName());
        editor.putString("MemberDueDate", user.getDueDate());
        editor.putString("Credits", String.valueOf(user.getCredits()));
        editor.putString("Version", user.getVersion());
        editor.putString("HeadPic",user.getHeadPic());
        if (rememberpwd.isChecked()) {
            editor.putString("Password", user.getPassWord());
            editor.putBoolean("rememberpwd",true);
        }

        editor.commit();

        NetApp.mInstance.setUser(user);

        Log.d("Login info ", "user info is saved");
    }
}
