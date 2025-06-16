package com.air.airspeed.bottombar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class BottomBar extends View {

    private Context context;

    public BottomBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    //////////////////////////////////////////////////
    //提供的api 并且根据api做一定的物理基础准备
    //////////////////////////////////////////////////

    private int containerId;

    private List<Class> fragmentClassList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private List<Integer> iconResBeforeList = new ArrayList<>();
    private List<Integer> iconResAfterList = new ArrayList<>();

    private List<Fragment> fragmentList = new ArrayList<>();

    private int itemCount;

    private Paint paint = new Paint();

    private List<Bitmap> iconBitmapBeforeList = new ArrayList<>();
    private List<Bitmap> iconBitmapAfterList = new ArrayList<>();
    private List<Rect> iconRectList = new ArrayList<>();

    private int currentCheckedIndex;
    private int firstCheckedIndex;

    private int ballcount = -1;
    private Point ballpoint = new Point();

    private int titleColorBefore = Color.parseColor("#999999");
    private int titleColorAfter = Color.parseColor("#ff5d5e");

    private int titleSizeInDp = 10;
    private int iconWidth = 20;
    private int iconHeight = 20;
    private int titleIconMargin = 5;


    public void setBallcount(int ballcount) {
        this.ballcount = ballcount;
    }
    public Point getBallPoint() {
        return ballpoint;
    }
    public BottomBar setContainer(int containerId) {
        this.containerId = containerId;
        return this;
    }

    public BottomBar setTitleBeforeAndAfterColor(String beforeResCode, String AfterResCode) {//支持"#333333"这种形式
        titleColorBefore = Color.parseColor(beforeResCode);
        titleColorAfter = Color.parseColor(AfterResCode);
        return this;
    }

    public BottomBar setTitleSize(int titleSizeInDp) {
        this.titleSizeInDp = titleSizeInDp;
        return this;
    }

    public BottomBar setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
        return this;
    }

    public BottomBar setTitleIconMargin(int titleIconMargin) {
        this.titleIconMargin = titleIconMargin;
        return this;
    }

    public BottomBar setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
        return this;
    }

    public BottomBar addItem(Class fragmentClass, String title, int iconResBefore, int iconResAfter) {
        fragmentClassList.add(fragmentClass);
        titleList.add(title);
        iconResBeforeList.add(iconResBefore);
        iconResAfterList.add(iconResAfter);
        return this;
    }

    public BottomBar setFirstChecked(int firstCheckedIndex) {//从0开始
        this.firstCheckedIndex = firstCheckedIndex;
        return this;
    }

    /*public void getIconPosition(int count, Point point) {  //从0开始
        Rect tmprect = iconRectList.get(count);
        point.x = tmprect.right;
        point.y = tmprect.top;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void build() {
        itemCount = fragmentClassList.size();
        //预创建bitmap的Rect并缓存
        //预创建icon的Rect并缓存
        for (int i = 0; i < itemCount; i++) {
            Bitmap beforeBitmap = getBitmap(iconResBeforeList.get(i));
            iconBitmapBeforeList.add(beforeBitmap);

            Bitmap afterBitmap = getBitmap(iconResAfterList.get(i));
            iconBitmapAfterList.add(afterBitmap);

            Rect rect = new Rect();
            iconRectList.add(rect);

            Class clx = fragmentClassList.get(i);
            try {
                Fragment fragment = (Fragment) clx.newInstance();
                fragmentList.add(fragment);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        currentCheckedIndex = firstCheckedIndex;
        switchFragment(currentCheckedIndex);

        invalidate();
    }

    private Bitmap getBitmap(int resId) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(resId);
        return bitmapDrawable.getBitmap();
    }

    //////////////////////////////////////////////////
    //初始化数据基础
    //////////////////////////////////////////////////

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initParam();
    }

    private int titleBaseLine;
    private List<Integer> titleXList = new ArrayList<>();

    private int parentItemWidth;

    private void initParam() {
        if (itemCount != 0) {
            //单个item宽高
            parentItemWidth = getWidth() / itemCount;
            int parentItemHeight = getHeight();

            //图标边长
            int iconWidth = dp2px(this.iconWidth);//先指定20dp
            int iconHeight = dp2px(this.iconHeight);

            //图标文字margin

            int textIconMargin = dp2px(((float)titleIconMargin)/2);//先指定5dp，这里除以一半才是正常的margin，不知道为啥，可能是图片的原因

            //标题高度
            int titleSize = dp2px(titleSizeInDp);//这里先指定10dp
            paint.setTextSize(titleSize);
            Rect rect = new Rect();
            paint.getTextBounds(titleList.get(0), 0, titleList.get(0).length(), rect);
            int titleHeight = rect.height();

            //从而计算得出图标的起始top坐标、文本的baseLine
            int iconTop = (parentItemHeight - iconHeight - textIconMargin - titleHeight)/2;
            titleBaseLine = parentItemHeight - iconTop;

            //对icon的rect的参数进行赋值
            int firstRectX = (parentItemWidth - iconWidth) / 2;//第一个icon的左
            for (int i = 0; i < itemCount; i++) {
                int rectX = i * parentItemWidth + firstRectX;

                Rect temp = iconRectList.get(i);

                temp.left = rectX;
                temp.top = iconTop ;
                temp.right = rectX + iconWidth;
                temp.bottom = iconTop + iconHeight;
            }

            //标题（单位是个问题）
            for (int i = 0; i < itemCount; i ++) {
                String title = titleList.get(i);
                paint.getTextBounds(title, 0, title.length(), rect);
                titleXList.add((parentItemWidth - rect.width()) / 2 + parentItemWidth * i);
            }
        }
    }

    private int dp2px(float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //////////////////////////////////////////////////
    //根据得到的参数绘制
    //////////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);//这里让view自身替我们画背景 如果指定的话

        if (itemCount != 0) {
            //画背景
            paint.setAntiAlias(false);
            for (int i = 0; i < itemCount; i++) {
                Bitmap bitmap = null;
                if (i == currentCheckedIndex) {
                    bitmap = iconBitmapAfterList.get(i);
                } else {
                    bitmap = iconBitmapBeforeList.get(i);
                }
                Rect rect = iconRectList.get(i);
                canvas.drawBitmap(bitmap, null, rect, paint);//null代表bitmap全部画出

                if (i == ballcount) {
                    ballpoint.x = rect.right;
                    ballpoint.y = rect.top;

                }
            }

            //画文字
            paint.setAntiAlias(true);
            for (int i = 0; i < itemCount; i ++) {
                String title = titleList.get(i);
                if (i == currentCheckedIndex) {
                    paint.setColor(titleColorAfter);
                } else {
                    paint.setColor(titleColorBefore);
                }
                int x = titleXList.get(i);
                canvas.drawText(title, x, titleBaseLine, paint);
            }
        }
    }



    //////////////////////////////////////////////////
    //点击事件:我观察了微博和掌盟，发现down和up都在该区域内才响应
    //////////////////////////////////////////////////

    int target = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                target = withinWhichArea((int)event.getX());
                break;
            case MotionEvent.ACTION_UP :
                if (event.getY() < 0) {
                    break;
                }
                if (target == withinWhichArea((int)event.getX())) {
                    //这里触发点击事件
                    switchFragment(target);
                    currentCheckedIndex = target;
                    invalidate();
                }
                target = -1;
                break;
        }
        return true;
        //这里return super为什么up执行不到？是因为return super的值，全部取决于你是否
        //clickable，当你down事件来临，不可点击，所以return false，也就是说，而且你没
        //有设置onTouchListener，并且控件是ENABLE的，所以dispatchTouchEvent的返回值
        //也是false，所以在view group的dispatchTransformedTouchEvent也是返回false，
        //这样一来，view group中的first touch target就是空的，所以intercept标记位
        //果断为false，然后就再也进不到循环取孩子的步骤了，直接调用dispatch-
        // TransformedTouchEvent并传孩子为null，所以直接调用view group自身的dispatch-
        // TouchEvent了
    }

    private int withinWhichArea(int x) { return x/parentItemWidth; }//从0开始

    //////////////////////////////////////////////////
    //碎片处理代码
    //////////////////////////////////////////////////
    private Fragment currentFragment;

    //注意 这里是只支持AppCompatActivity 需要支持其他老版的 自行修改
    protected void switchFragment(int whichFragment) {
        Fragment fragment = fragmentList.get(whichFragment);
        int frameLayoutId = containerId;

        if (fragment != null) {
            FragmentTransaction transaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction();
            if (fragment.isAdded()) {
                if (currentFragment != null) {
                    transaction.hide(currentFragment).show(fragment);
                } else {
                    transaction.show(fragment);
                }
            } else {
                if (currentFragment != null) {
                    transaction.hide(currentFragment).add(frameLayoutId, fragment);
                } else {
                    transaction.add(frameLayoutId, fragment);
                }
            }
            currentFragment = fragment;
            transaction.commit();
        }
    }
}
