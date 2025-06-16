package com.air.airspeed;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import static android.widget.Toast.LENGTH_LONG;

public class ModPhoneActivity extends AppCompatActivity {

    ImageView backarrow;
    EditText oldaccount, oldpass, newaccount, newcode;
    Button modphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_phone);

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        oldaccount = (EditText) findViewById(R.id.oldaccount);
        oldpass = (EditText) findViewById(R.id.oldpass);
        newaccount = (EditText) findViewById(R.id.newaccount);
        newcode = (EditText) findViewById(R.id.newcode);
        modphone = (Button) findViewById(R.id.modphone);
    }

    public void setClick() {
        backarrow.setOnClickListener(new ModPhoneListener());
        modphone.setOnClickListener(new ModPhoneListener());
    }

    class ModPhoneListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(ModPhoneActivity.this,UserInfo.class);
                    startActivity(intent1);
                    break;
                case R.id.modphone:
                    modifyphone();
                    break;
                default:
                    break;
            }
        }
    }

    private void modifyphone() {
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("OldUserName", oldaccount.getText().toString());
        mFormBodyBuild.add("OldPassword", oldpass.getText().toString());
        mFormBodyBuild.add("NewUserName",newaccount.getText().toString());
        mFormBodyBuild.add("NewCode", newcode.getText().toString());

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/ModifyPhoneServlet")
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

                        ModPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModPhoneActivity.this,"新手机绑定成功",Toast.LENGTH_LONG).show();
                            }
                        });
                        Intent intent = new Intent(ModPhoneActivity.this, LoginSuccessActivity.class);
                        startActivity(intent);
                    }else {
                        ModPhoneActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(ModPhoneActivity.this);
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

                    ModPhoneActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ModPhoneActivity.this);
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

                ModPhoneActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(ModPhoneActivity.this);
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
        editor.putBoolean("rememberpwd",true);
        editor.putString("Password", user.getPassWord());
        editor.putString("MemberDueDate", user.getDueDate());
        editor.putString("Credits", String.valueOf(user.getCredits()));
        editor.putString("Version", user.getVersion());
        editor.commit();


        NetApp.mInstance.setUser(user);
    }


}
