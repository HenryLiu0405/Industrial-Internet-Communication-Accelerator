package com.air.airspeed;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.nio.channels.spi.SelectorProvider;

public class SelectBuyTime extends AppCompatActivity {
    private TextViewBorder buyHour;
    private TextViewBorder buyMonth;
    private String buyTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_buy_time);
        buyHour = (TextViewBorder) findViewById(R.id.hour);
        buyMonth = (TextViewBorder) findViewById(R.id.month);
        Button buttonBuy = (Button) findViewById(R.id.button_buy);

        //1. get Speed APP  2. change textview selected border  3. button event

        //2. change textview selected border  3. button event
        buyHour.setOnClickListener(new BuyTimeClick());
        buyMonth.setOnClickListener(new BuyTimeClick());
        buttonBuy.setOnClickListener(new BuyTimeClick());
    }

        class BuyTimeClick implements View.OnClickListener{


            @Override
            public void onClick(View v)  {
                switch(v.getId()) {
                    case R.id.hour:
                        buyTime = "buyHour";
                        //buyHour.setBorderColor(Color.BLACK);
                        //buyHour.setBorderColor(R.color.red);
                        //buyHour.setBorderColor(ContextCompat.getColor(this, R.color.black));
                        buyHour.setBorderColor(ContextCompat.getColor(SelectBuyTime.this,R.color.red));
                        buyMonth.setBorderColor(ContextCompat.getColor(SelectBuyTime.this, R.color.gray));
                        break;
                    case R.id.month:
                        buyTime = "buyMonth";
                        buyMonth.setBorderColor(ContextCompat.getColor(SelectBuyTime.this, R.color.red));
                        buyHour.setBorderColor(ContextCompat.getColor(SelectBuyTime.this,R.color.gray));
                        //buyMonth.setBorderColor(Color.argb(0xff, 0x00, 0x00, 0x00));
                        break;
                    case R.id.button_buy:
                        //1. get Speed APP
                        Intent intent2 = getIntent();
                        Bundle bd2 = intent2.getExtras();

                        Intent intent3 = new Intent(SelectBuyTime.this, SelectPayType.class);

                        bd2.putString("butTime", buyTime);
                        intent3.putExtras(bd2);
                        startActivity(intent3);
                        break;

                }
            }
        }





}
