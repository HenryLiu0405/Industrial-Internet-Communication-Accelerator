package com.air.airspeed;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {
    private ImageView backarrow;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        message = (TextView) findViewById(R.id.message);
    }

    public void setClick() {
        backarrow.setOnClickListener(new MessageClick());
    }

    class MessageClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(MessageActivity.this, LoginSuccessActivity.class);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    }
}
