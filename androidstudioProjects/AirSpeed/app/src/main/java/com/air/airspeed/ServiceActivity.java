package com.air.airspeed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ServiceActivity extends AppCompatActivity {
    private ImageView backarrow;
    private TextView servicephone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        servicephone = (TextView) findViewById(R.id.servicephone);
    }

    public void setClick() {
        backarrow.setOnClickListener(new ServiceClick());
    }

    class ServiceClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(ServiceActivity.this, LoginSuccessActivity.class);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    }
}
