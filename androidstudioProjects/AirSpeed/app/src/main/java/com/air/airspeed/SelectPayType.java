package com.air.airspeed;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.BroadcastReceiver;

public class SelectPayType extends AppCompatActivity implements View.OnClickListener{
    private RadioGroup radioGroup;
    private RadioButton alipay;
    private RadioButton wechat;
    private int payType = 0;
    private Button pay;
    private String buytime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pay_type);

        Intent intent = getIntent();
        buytime = intent.getStringExtra("buytime");

        initView();

        radioGroup = (RadioGroup)findViewById(R.id.payradio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (alipay.getId()== i){
                    payType = 1;
                }
                if (wechat.getId()== i){
                    payType = 2;
                }
            }
        });

    }

    public void initView() {
        findViewById(R.id.paytype).setOnClickListener(this);
        findViewById(R.id.text).setOnClickListener(this);

        alipay = (RadioButton)findViewById(R.id.alipay);
        Drawable aliDra = ContextCompat.getDrawable(SelectPayType.this,R.drawable.alipay);
        aliDra.setBounds(0,0,100,100);
        alipay.setCompoundDrawables(aliDra,null,null,null);

        wechat = (RadioButton)findViewById(R.id.wechat);
        Drawable weDra = ContextCompat.getDrawable(SelectPayType.this,R.drawable.wechat);
        weDra.setBounds(0,0,100,100);
        wechat.setCompoundDrawables(weDra,null,null,null);
    }

    @Override
    public void onClick(View view)  {
        switch (view.getId()) {
            case R.id.paytype:
                if (payType == 0){
                    Toast.makeText(this, "请选择支付方式", Toast.LENGTH_LONG).show();
                    break;
                }

                // connect pay server, later add  2018.09.20

                //now, after click button and pay sucessfully, send  5 element sets of ip/tcp stream

        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(this,  "已经收到广播", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "已经收到广播", Toast.LENGTH_LONG).show();
            Toast.makeText(context, "已经收到广播", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetFile.MSG_NET_GET);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

}
