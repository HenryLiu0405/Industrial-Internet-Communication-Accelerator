package com.air.airspeed;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {
    private ImageView backarrow;
    private TextView score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        score = (TextView) findViewById(R.id.score);
        int credits = NetApp.mInstance.getUser().getCredits();
        String scoreshow = "您当前的积分是：" + String.valueOf(credits) + "分  ヾ(◍°∇°◍)ﾉﾞ加油哟！";
        score.setText(scoreshow);
        score.setTextColor(Color.BLACK);
    }

    public void setClick() {
        backarrow.setOnClickListener(new  ScoreListener());
    }

    class ScoreListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(ScoreActivity.this,LoginSuccessActivity.class);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    }
}
