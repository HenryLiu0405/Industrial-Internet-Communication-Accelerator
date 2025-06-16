package com.air.airspeed;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.ByteArrayInputStream;

import static com.air.airspeed.R.*;

public class MemberCenterActivity extends AppCompatActivity {
    private TextViewBorder hour, month, season, halfyear;
    private TextView phone,date;
    private Button buy;
    private ImageView backarrow,head;
    private String activity;
    private String buytime = "month";
    private LinearLayout layout;
    private PopupWindow popupWindow;

    private SpannableString shour = new SpannableString("1小时               1  元");
    private SpannableString smonth = new SpannableString("30 天               5  元");
    private SpannableString sseason = new SpannableString("90 天        15元");
    private SpannableString shalfyear = new SpannableString("180天      20元");

    private RelativeSizeSpan sp = new RelativeSizeSpan(1.7f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_center);

        Intent intent = getIntent();
        activity = intent.getStringExtra("activity");

        //设置变大字体SpannableString
        initView();
        setClick();
    }

    public void initView() {
        backarrow = (ImageView) findViewById(id.backarrow);
        head = (ImageView) findViewById(id.head);
        phone = (TextView) findViewById(id.phone);
        date = (TextView) findViewById(id.date);
        hour = (TextViewBorder) findViewById(id.hour);
        month = (TextViewBorder) findViewById(id.month);
        season = (TextViewBorder) findViewById(id.season);
        halfyear = (TextViewBorder) findViewById(id.halfyear);
        buy = (Button) findViewById(id.buy);
        setView();
    }

    @SuppressLint("ResourceAsColor")
    public void setView() {
        ForegroundColorSpan white = new ForegroundColorSpan(Color.WHITE);
        ForegroundColorSpan mycolor = new ForegroundColorSpan(Color.parseColor("#6A5ACD"));

        date.setText(NetApp.mInstance.getUser().getDueDate());

        shour.setSpan(sp,18,19,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        hour.setText(shour);

        smonth.setSpan(sp,19,20, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        smonth.setSpan(white,0, smonth.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        month.setText(smonth);

        month.setBackground(color.mycolor);

        sseason.setSpan(sp,12,14,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        season.setText(sseason);
        shalfyear.setSpan(sp,10,12,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        halfyear.setText(shalfyear);

        String yy = NetApp.mInstance.getUser().getDueDate();
        if (!yy.equals(null)) {
            yy = yy.substring(0,10);
            phone.setText(NetApp.mInstance.getUser().getUserName());
            date.setText(yy);
            date.setTextColor(Color.WHITE);
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
        backarrow.setOnClickListener(new memberClick());
        hour.setOnClickListener(new memberClick());
        month.setOnClickListener(new memberClick());
        season.setOnClickListener(new memberClick());
        halfyear.setOnClickListener(new memberClick());
        buy.setOnClickListener(new memberClick());
        head.setOnClickListener(new memberClick());
        phone.setOnClickListener(new memberClick());
        date.setOnClickListener(new memberClick());
    }

    class memberClick implements View.OnClickListener {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onClick(View v) {
            ForegroundColorSpan white = new ForegroundColorSpan(Color.WHITE);
            ForegroundColorSpan mycolor = new ForegroundColorSpan(Color.parseColor("#6A5ACD"));
            switch (v.getId()) {
                case id.backarrow:
                    if (activity.equals("LoginSuccess")) {
                        Intent intent1 = new Intent(MemberCenterActivity.this,LoginSuccessActivity.class);
                        startActivity(intent1);
                    }else if (activity.equals("UserInfo")) {
                        Intent intent2 = new Intent(MemberCenterActivity.this,UserInfo.class);
                        startActivity(intent2);
                    }else {
                        Intent intent3 = new Intent(MemberCenterActivity.this,LoginSuccessActivity.class);
                        startActivity(intent3);
                    }
                    break;
                case id.hour:
                    hour.setBackground(color.mycolor);
                    shour.setSpan(white,0, shour.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    hour.setText(shour);

                    month.setBackground(color.white);
                    month.setBorderColor(color.mycolor);
                    smonth.setSpan(mycolor,0, smonth.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    month.setText(smonth);

                    season.setBackground(color.white);
                    season.setBorderColor(color.mycolor);
                    sseason.setSpan(mycolor,0, sseason.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    season.setText(sseason);

                    halfyear.setBackground(color.white);
                    halfyear.setBorderColor(color.mycolor);
                    shalfyear.setSpan(mycolor,0, shalfyear.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    halfyear.setText(shalfyear);
                    buytime = "hour";
                    break;
                case id.month:
                    month.setBackground(color.mycolor);
                    smonth.setSpan(white,0, smonth.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    month.setText(smonth);

                    hour.setBackground(color.white);
                    hour.setBorderColor(color.mycolor);
                    shour.setSpan(mycolor,0, shour.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    hour.setText(shour);

                    season.setBackground(color.white);
                    season.setBorderColor(color.mycolor);
                    sseason.setSpan(mycolor,0, sseason.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    season.setText(sseason);

                    halfyear.setBackground(color.white);
                    halfyear.setBorderColor(color.mycolor);
                    shalfyear.setSpan(mycolor,0, shalfyear.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    halfyear.setText(shalfyear);
                    buytime = "month";
                    break;
                case id.season:
                    season.setBackground(color.mycolor);
                    sseason.setSpan(white,0, sseason.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    season.setText(sseason);


                    hour.setBackground(color.white);
                    hour.setBorderColor(color.mycolor);
                    shour.setSpan(mycolor,0, shour.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    hour.setText(shour);

                    month.setBackground(color.white);
                    month.setBorderColor(color.mycolor);
                    smonth.setSpan(mycolor,0, smonth.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    month.setText(smonth);

                    halfyear.setBackground(color.white);
                    halfyear.setBorderColor(color.mycolor);
                    shalfyear.setSpan(mycolor,0, shalfyear.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    halfyear.setText(shalfyear);
                    buytime = "season";
                    break;
                case id.halfyear:
                    halfyear.setBackground(color.mycolor);
                    shalfyear.setSpan(white,0, shalfyear.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    halfyear.setText(shalfyear);

                    hour.setBackground(color.white);
                    hour.setBorderColor(color.mycolor);
                    shour.setSpan(mycolor,0, shour.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    hour.setText(shour);

                    season.setBackground(color.white);
                    season.setBorderColor(color.mycolor);
                    sseason.setSpan(mycolor,0, sseason.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    season.setText(sseason);

                    month.setBackground(color.white);
                    month.setBorderColor(color.mycolor);
                    smonth.setSpan(mycolor,0, smonth.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    month.setText(smonth);
                    buytime = "halfyear";
                    //文字变白色，其他框恢复白色
                    break;
                case id.buy:
                    //Intent intent4 = new Intent(MemberCenterActivity.this, SelectPayType.class);
                    //intent4.putExtra("buytime", buytime);
                    //startActivity(intent4);
                    showbuttomwidow(v);
                    break;
                case id.head:
                case id.phone:
                case id.date:
                    Intent intent4 = new Intent(MemberCenterActivity.this,LoginActivity.class);
                    startActivity(intent4);
                    break;
                default:
                    break;
            }
        }
    }

    public void showbuttomwidow(View view)  {

        layout = (LinearLayout) getLayoutInflater().inflate(R.layout.paytype,null);
        popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白隐藏pop
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入动画
        popupWindow.setAnimationStyle(style.Popupwindow);
        int[]  location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT|Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
        setClickListener(layout);
        //添加pop关闭事件，实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new poponDismissListener());
        backgroundAlpha(0.5f);


    }

    private void setClickListener(LinearLayout layout) {
        TextView wechatpay = (TextView) layout.findViewById(id.wechat);
        TextView alipay = (TextView) layout.findViewById(id.alipay);

        wechatpay.setOnClickListener(new payListener());
        alipay.setOnClickListener(new payListener());
    }
    class payListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId())  {
                case id.wechat:
                    //联接微信支付，下面内部测试
                    Intent intent4 = new Intent(MemberCenterActivity.this, SelectPayType.class);
                    intent4.putExtra("buytime", buytime);
                    startActivity(intent4);
                    popupWindow.dismiss();
                    break;
                case id.alipay:
                    //联接支付宝
                    popupWindow.dismiss();
                    break;
            }
        }
    }

    public void backgroundAlpha(float alpha)  {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getWindow().setAttributes(layoutParams);
    }

    class poponDismissListener implements PopupWindow.OnDismissListener {
        @Override
        public void onDismiss() {
            backgroundAlpha(1f);
        }
    }



}

