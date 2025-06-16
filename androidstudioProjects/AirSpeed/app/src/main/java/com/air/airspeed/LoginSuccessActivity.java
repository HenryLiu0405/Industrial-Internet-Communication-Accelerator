package com.air.airspeed;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.ETC1;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.air.airspeed.bottombar.BottomBar;
import com.air.airspeed.bottombar.fragment.Fragment1;
import com.air.airspeed.bottombar.fragment.Fragment2;
import com.air.airspeed.bottombar.fragment.Fragment3;

import java.io.ByteArrayInputStream;

public class LoginSuccessActivity extends AppCompatActivity {

    TextView phone,member;
    ImageView star,service, score, msg, conf,head;
    TextView onlineservice, getscore, message, config;
    ImageView memarr, servicearrow, scorearrow, msgarrow, confarrow;
    RelativeLayout relativeLayout1,relativeLayout2,relativeLayout3,relativeLayout4;

    @SuppressLint("ClickableViewAccessibility")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.f1_container)
                .setTitleBeforeAndAfterColor("#999999","#ff5d5e")
                .setFirstChecked(2)
                .setTitleSize(12)
                .setTitleIconMargin(1)
                .setIconHeight(40)
                .setIconWidth(40)
                .addItem(Fragment1.class,"应用",R.drawable.item1_before,R.drawable.item1_after)
                .addItem(Fragment2.class,"加速",R.drawable.item2_before,R.drawable.item2_after)
                .addItem(Fragment3.class,"我的",R.drawable.item3_before,R.drawable.item3_after)
                .build();
        bottomBar.setOnTouchListener(new BottomTouchListener());

        initView();
        //Intent intent = getIntent();
        //String username = intent.getStringExtra("UserName");
        setClick();

        //Toast.makeText(LoginSuccessActivity.this, "登录成功",Toast.LENGTH_LONG).show();
    }

    class BottomTouchListener implements View.OnTouchListener  {
        int target = -1;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int viewWidth = view.getWidth()/3;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    target = (int) event.getX()/viewWidth;
                    break;
                case MotionEvent.ACTION_UP:
                    if (event.getY() < 0) {
                        break;
                    }
                    if (target == ((int) event.getX()/viewWidth)) {
                        switch (target) {
                            case 0:
                                Intent intent = new Intent(LoginSuccessActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                            case 1:
                                Intent intent1 = new Intent(LoginSuccessActivity.this, MainSpeedActivity.class);
                                startActivity(intent1);
                                break;
                        }
                    }
                    break;
            }
            return false;
        }
    }

    public void initView() {
        head = (ImageView) findViewById(R.id.head);
        phone = (TextView) findViewById(R.id.phone);
        star = (ImageView) findViewById(R.id.star);
        member = (TextView) findViewById(R.id.member);
        memarr = (ImageView) findViewById(R.id.memarr);
        service = (ImageView) findViewById(R.id.service);
        onlineservice = (TextView) findViewById(R.id.onlineservice);
        servicearrow = (ImageView) findViewById(R.id.servicearrow);
        score = (ImageView) findViewById(R.id.score);
        getscore = (TextView) findViewById(R.id.getscore);
        scorearrow = (ImageView) findViewById(R.id.scorearrow);
        msg = (ImageView) findViewById(R.id.msg);
        message = (TextView) findViewById(R.id.message);
        msgarrow = (ImageView) findViewById(R.id.msgarrow);
        conf = (ImageView) findViewById(R.id.conf);
        config = (TextView) findViewById(R.id.config);
        confarrow = (ImageView) findViewById(R.id.confarrow);

        relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.relativeLayout2);
        relativeLayout3 = (RelativeLayout) findViewById(R.id.relativeLayout3);
        relativeLayout4 = (RelativeLayout) findViewById(R.id.relativeLayout4);

        if (NetApp.mInstance.getUser().getUserName().equals("")) {
            phone.setText("登录/注册");
            phone.setTextColor(Color.WHITE);
            phone.setTextSize(18);
        }else {
            phone.setText(NetApp.mInstance.getUser().getUserName());
        }

        //设置头像
        //1，取出字符串形式的bitmap
        String imageString = NetApp.mInstance.getUser().getHeadPic();
        //2，利用Base64将字符串转换成ByteArrayInputStream
        byte[] byteArray= Base64.decode(imageString,Base64.DEFAULT);
        if (!imageString.equals("null")) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
            //3，利用ByteArrayInputStream生成Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream);
            head.setImageBitmap(bitmap);
        }else {
            head.setImageResource(R.drawable.head);
        }

    }

    public void setClick() {
        head.setOnClickListener(new ClickListener());
        phone.setOnClickListener(new ClickListener());
        star.setOnClickListener(new ClickListener());
        member.setOnClickListener(new ClickListener());
        memarr.setOnClickListener(new ClickListener());
        service.setOnClickListener(new ClickListener());
        onlineservice.setOnClickListener(new ClickListener());
        servicearrow.setOnClickListener(new ClickListener());
        score.setOnClickListener(new ClickListener());
        getscore.setOnClickListener(new ClickListener());
        scorearrow.setOnClickListener(new ClickListener());
        msg.setOnClickListener(new ClickListener());
        message.setOnClickListener(new ClickListener());
        msgarrow.setOnClickListener(new ClickListener());
        conf.setOnClickListener(new ClickListener());
        config.setOnClickListener(new ClickListener());
        confarrow.setOnClickListener(new ClickListener());

        relativeLayout1.setOnClickListener(new ClickListener());
        relativeLayout2.setOnClickListener(new ClickListener());
        relativeLayout3.setOnClickListener(new ClickListener());
        relativeLayout4.setOnClickListener(new ClickListener());
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.head:
                case R.id.phone:
                    if (phone.getText().equals("登录/注册")) {
                        Intent intent1 = new Intent(LoginSuccessActivity.this,LoginActivity.class);
                        startActivity(intent1);
                    }else {
                        Intent intent = new Intent(LoginSuccessActivity.this,UserInfo.class);
                        startActivity(intent);
                    }

                    break;
                case R.id.star:
                case R.id.member:
                case R.id.memarr:
                    Intent intent2 = new Intent(LoginSuccessActivity.this,MemberCenterActivity.class);
                    intent2.putExtra("activity","LoginSuccess");
                    startActivity(intent2);
                    break;
                case R.id.relativeLayout1:
                case R.id.service:
                case R.id.onlineservice:
                case R.id.servicearrow:
                    Intent intent3 = new Intent(LoginSuccessActivity.this, ServiceActivity.class);
                    startActivity(intent3);
                    break;
                case R.id.relativeLayout2:
                case R.id.score:
                case R.id.getscore:
                case R.id.scorearrow:
                    Intent intent4 = new Intent(LoginSuccessActivity.this,ScoreActivity.class);
                    startActivity(intent4);
                    break;
                case R.id.relativeLayout3:
                case R.id.msg:
                case R.id.message:
                case R.id.msgarrow:
                    Intent intent5 = new Intent(LoginSuccessActivity.this,MessageActivity.class);
                    startActivity(intent5);
                    break;
                case R.id.relativeLayout4:
                case R.id.conf:
                case R.id.config:
                case R.id.confarrow:
                    Intent intent6 = new Intent(LoginSuccessActivity.this,ConfigActivity.class);
                    startActivity(intent6);
                    break;
                default:
                    break;
            }
        }
    }
}
