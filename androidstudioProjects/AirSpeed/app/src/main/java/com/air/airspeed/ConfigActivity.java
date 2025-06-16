package com.air.airspeed;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import static java.lang.Thread.sleep;

public class ConfigActivity extends AppCompatActivity {
    private ImageView backarrow;
    private TextView version,lastversion;

    private String netversion;

    private boolean FLAG_PERMISSION = false;
    private List<String> list;

    private final static int MOBILE = 0;
    private final static int UNICOM = 1;
    private final static int TELECOM =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        version = (TextView) findViewById(R.id.version);
        lastversion = (TextView) findViewById(R.id.lastversion);

        //netversion = NetApp.mInstance.getUser().getNetVerion();
        netversion = "2.0.0";
        version.setText("当前版本："+ NetApp.mInstance.getUser().getVersion());
        if (netversion.equals(NetApp.mInstance.getUser().getVersion())) {
            lastversion.setText("已经是最新版本");
        }else {
            lastversion.setText("更新版本");
            lastversion.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);;
            lastversion.getPaint().setAntiAlias(true);
        }

    }

    public void setClick() {
        backarrow.setOnClickListener(new ConfigClick());
        lastversion.setOnClickListener(new ConfigClick());

    }

    class ConfigClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(ConfigActivity.this, LoginSuccessActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.lastversion:
                    AlertDialog.Builder alert = new AlertDialog.Builder(ConfigActivity.this);
                    alert.setTitle("版本升级")
                            .setMessage("升级到最新版本" + netversion
                            )
                            .setPositiveButton("立即下载",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            obtainPermission();

                                        }
                                    });
                    alert.create().show();
                    break;

                default:
                    break;
            }
        }
    }

    private void obtainPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            list = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.INTERNET);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (list.size()!=0) {
                requestPermissions(list.toArray(new String[list.size()]),1);
            }
        }else {
            FLAG_PERMISSION = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.d("test", Arrays.toString(grantResults));
        for (int i=0; i<grantResults.length;i++) {
            if (grantResults[i] == -1) {
                FLAG_PERMISSION = false;
                break;
            }
        }
        FLAG_PERMISSION = true;
        Intent updateIntent = new Intent(ConfigActivity.this,UpdateService.class);
        updateIntent.putExtra("titleId",R.string.app_name);
        updateIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startService(updateIntent);
    }


}
