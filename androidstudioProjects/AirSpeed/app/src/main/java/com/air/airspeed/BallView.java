package com.air.airspeed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

public class BallView extends android.support.v7.widget.AppCompatTextView implements ValueAnimator.AnimatorUpdateListener{
    public static final int VIEW_SIZE = 14;

    protected Context mContext;
    protected Paint mPaint4Circle;
    protected int radius;

    protected Point startPosition;
    protected Point endPosition;
    protected int number;

    public BallView(Context context,int number) {
        this(context, null);
        this.number = number;
    }

    public BallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallView(Context context, AttributeSet attrs, int defStyleAttr)  {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        //初始化画笔
        mPaint4Circle = new Paint();
        mPaint4Circle.setColor(Color.RED);
        mPaint4Circle.setAntiAlias(true);

        setGravity(Gravity.CENTER);
        setText("1");
        setTextColor(Color.WHITE);
        setTextSize(8);
    }

    public void setStartPosition(Point startPosition) {
        startPosition.y = startPosition.y - 10;
        this.startPosition = startPosition;
    }

    public void setEndPosition(Point endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int pxSize = (int) convertDpToPixel(VIEW_SIZE, mContext);
        //绘制大小
        setMeasuredDimension(pxSize, pxSize);
        //求出半径
        radius = pxSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画一个圆
        canvas.drawCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, radius, mPaint4Circle);
        super.onDraw(canvas);
    }

    public void startBeizerAnimation() {
            if (startPosition == null || endPosition == null) {
                return;
            }

            //绘制一个贝塞尔曲线的控制球，默认每个往上飞100
            int pointX = (startPosition.x + endPosition.x)/2;
            int pointY = (int) (startPosition.y - convertDpToPixel(100, mContext));
            Point controllPoint = new Point(pointX, pointY);

            BezierEvaluator bezierEvaluator = new BezierEvaluator(controllPoint);
            ValueAnimator anim = ValueAnimator.ofObject(bezierEvaluator, startPosition, endPosition);
            anim.addUpdateListener(this);
            anim.setDuration(700);  //飞行1000ms
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setText(String.valueOf(number));
                    //ViewGroup viewGroup = (ViewGroup) getParent();
                    //动画完后移除view
                    //viewGroup.removeView(BallView.this);
                }
            });
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        Point point = (Point) animation.getAnimatedValue();
        setX(point.x);
        setY(point.y);
        invalidate();
    }

    public class BezierEvaluator implements TypeEvaluator<Point> {
        private Point controllPoint;

        public BezierEvaluator(Point controllPoint) {
            this.controllPoint = controllPoint;
        }

        @Override
        public Point evaluate(float t, Point startValue, Point endValue) {
            int x = (int) ((1-t)*(1-t)*startValue.x + 2*t*(1-t)*controllPoint.x + t*t*endValue.x);
            int y = (int) ((1-t)*(1-t)*startValue.y + 2*t*(1-t)*controllPoint.y + t*t*endValue.y);
            return new Point(x, y);
        }
    }

    //dp转换px
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}


























