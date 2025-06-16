package com.air.airspeed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

public class RegisterActivity extends AppCompatActivity {
    private EditText account,password;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        setClick();
        //UserName and Password need save to sharedpreferences "UserInfo".xml

    }

    public void initView() {
        account = (EditText) findViewById(R.id.account);
        password = (EditText) findViewById(R.id.password);
        register = (Button) findViewById(R.id.register);
    }

    public void setClick() {
        register.setOnClickListener(new RegisterClick());
    }

    class RegisterClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register:
                    newuserregister();
                    break;
                default:
                    break;
            }
        }
    }

    public void newuserregister() {
        FormBody.Builder mFormBodyBuilder = new FormBody.Builder();
        mFormBodyBuilder.add("UserName",account.getText().toString());
        mFormBodyBuilder.add("Password",password.getText().toString());
        mFormBodyBuilder.add("MemberDueDate","");
        mFormBodyBuilder.add("Credits","0");
        mFormBodyBuilder.add("Version","0");

        FormBody mFormBody = mFormBodyBuilder.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/RegisterServlet")
                .post(mFormBody)
                .build();
        HttpUtil.sendOkHttpRequest(request,new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try{
                    String responseData = response.body().string();
                    JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                    String result = jsonObject.getString("Result");
                    if (result.equals("success")) {
                        User user = new User();
                        user.setUserName(jsonObject.getString("UserName"));
                        user.setPassWord(jsonObject.getString("Password"));
                        user.setDueDate(jsonObject.getString("MemberDueDate"));
                        user.setCredits(jsonObject.getInt("Credits"));
                        user.setVersion(jsonObject.getString("Version"));
                        saveInfo(user);
                        Intent intent = new Intent(RegisterActivity.this,LoginSuccessActivity.class);
                        startActivity(intent);
                    }else if(result.equals("failed")){
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage("注册不成功，请稍后再试");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,int arg1) {
                                        arg0.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }else if (result.equals("existed")) {
                        RegisterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage("该用户已存在");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener() {
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
                }catch (JSONException e) {
                    RegisterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setMessage("服务器返回异常，请稍后再试");
                            builder.setTitle("提示");
                            builder.setPositiveButton("确认",new android.content.DialogInterface.OnClickListener() {
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
            public void onFailure(Call call,IOException e) {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        builder.setMessage("网络无连接，请稍后再试");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
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
        editor.putBoolean("rememberpwd",true);
        editor.putString("Password", user.getPassWord());
        editor.commit();

        NetApp.mInstance.setUser(user);
    }
}
