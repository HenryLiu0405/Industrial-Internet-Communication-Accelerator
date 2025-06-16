package com.air.airspeed;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

/**
 * Created by lius on 2018/9/17.
 */

public class TextViewBorder extends AppCompatTextView {
    private static final int STROKE_WIDTH = 10;
    private int borderCol;
    private int fillColor;

    private Paint borderPaint;

    public TextViewBorder(Context context, AttributeSet attrs){
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,

                R.styleable.TextViewBorder,0,0);
        try {
            borderCol = a.getInteger(R.styleable.TextViewBorder_borderColor,0);
        }finally {
            a.recycle( );
        }

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(STROKE_WIDTH);
        borderPaint.setAntiAlias(true);  //设置防锯齿
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (0 == this.getText().toString().length())
            return;

        borderPaint.setColor(borderCol);


        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();

        RectF rect = new RectF(2,2,width-2, height-2);
        canvas.drawRoundRect(rect,50,50,borderPaint);
        super.onDraw(canvas);
    }

    public int getBorderColor(){
        return borderCol;
    }

    public void setBorderColor(int newColor) {
        borderCol = newColor;
        borderPaint.setStyle(Paint.Style.STROKE);
        invalidate();
        requestLayout();
    }

    public void setBackground(int fillColor) {
        this.fillColor = fillColor;
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(fillColor);
        invalidate();
        requestLayout();
    }



}
