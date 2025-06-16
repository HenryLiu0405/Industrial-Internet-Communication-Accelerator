package com.air.airspeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import static android.widget.Toast.LENGTH_LONG;
import static com.air.airspeed.R.drawable.head;

public class UserInfo extends AppCompatActivity {

    ImageView photoarrow, phonearrow, passarrow, datearrow, backarrow,headpic;
    Button logout;
    TextView phonenumber, date;
    RelativeLayout userinfo1,userinfo2,userinfo3,userinfo4;
    private LinearLayout poplayout;
    private PopupWindow popupWindow;
    private Bitmap head;
    private List<String> list;
    private Uri cameraUri,cropUri;
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/AirSpeed";

    private final static int CODE_ALBUM = 1;
    private final static int CODE_CAMERA = 2;
    private final static int CODE_CROP = 3;
    private final static int CODE_PERMISSION = 4;
    private boolean FLAG_PERMISSION = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initView();
        setClick();
    }

    @SuppressLint("WrongViewCast")
    public void initView() {
        backarrow = (ImageView) findViewById(R.id.backarrow);
        photoarrow = (ImageView) findViewById(R.id.photoarrow);
        phonearrow = (ImageView) findViewById(R.id.phonearrow);
        passarrow = (ImageView) findViewById(R.id.passarrow);
        datearrow = (ImageView) findViewById(R.id.datearrow);
        headpic = (ImageView) findViewById(R.id.headpic);

        phonenumber = (TextView) findViewById(R.id.phonenumber);
        date = (TextView) findViewById(R.id.date);

        logout = (Button) findViewById(R.id.logout);

        userinfo1 = (RelativeLayout) findViewById(R.id.userinfo1);
        userinfo2 = (RelativeLayout) findViewById(R.id.userinfo2);
        userinfo3 = (RelativeLayout) findViewById(R.id.userinfo3);
        userinfo4 = (RelativeLayout) findViewById(R.id.userinfo4);

        phonenumber.setText(NetApp.mInstance.getUser().getUserName());
        phonenumber.setTextColor(Color.BLACK);
        String yy = NetApp.mInstance.getUser().getDueDate();
        if (!yy.equals(null)) {
            yy = yy.substring(0,10);
            date.setText(yy);
            date.setTextColor(Color.BLACK);
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
            headpic.setImageBitmap(bitmap);
        }else {
            headpic.setImageResource(R.drawable.head);
        }
    }

    public void setClick() {
        backarrow.setOnClickListener(new UserInfoListener());
        photoarrow.setOnClickListener(new UserInfoListener());
        phonearrow.setOnClickListener(new UserInfoListener());
        passarrow.setOnClickListener(new UserInfoListener());
        datearrow.setOnClickListener(new UserInfoListener());

        logout.setOnClickListener(new UserInfoListener());

        userinfo1.setOnClickListener(new UserInfoListener());
        userinfo2.setOnClickListener(new UserInfoListener());
        userinfo3.setOnClickListener(new UserInfoListener());
        userinfo4.setOnClickListener(new UserInfoListener());
    }

    class UserInfoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backarrow:
                    Intent intent1 = new Intent(UserInfo.this, LoginSuccessActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.userinfo1:
                case R.id.photoarrow:
                    showbuttomwidow(v);
                    break;
                case R.id.userinfo2:
                case R.id.phonearrow:
                    Intent intent3 = new Intent(UserInfo.this,ModPhoneActivity.class);
                    startActivity(intent3);
                    break;
                case R.id.userinfo3:
                case R.id.passarrow:
                    Intent intent2 = new Intent(UserInfo.this, ForgetPwdActivity.class);
                    intent2.putExtra("activity","UserInfo");
                    startActivity(intent2);
                    break;
                case R.id.userinfo4:
                case R.id.datearrow:
                    Intent intent4 = new Intent(UserInfo.this,MemberCenterActivity.class);
                    intent4.putExtra("activity","UserInfo");
                    startActivity(intent4);
                    break;
                case R.id.logout:
                    //deleteDatabase()
                    deleteUserInfo(phonenumber.getText().toString());
                    Intent intent = new Intent(UserInfo.this, LoginActivity.class);
                    startActivity(intent);
            }
        }
    }

    public void deleteUserInfo(String username) {
        User user = new User();
        user = NetApp.mInstance.getUser();
        if (user.getUserName().equals(username)) {
            user.setUserName("");
            user.setPassWord("");
        }else {
            return;
        }
        NetApp.mInstance.setUser(user);

        SharedPreferences pref;
        SharedPreferences.Editor editor;

        pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean("isSaved", false);
        editor.putString("UserName", "");
        editor.putBoolean("rememberpwd",false);
        editor.putString("Password","");
        editor.commit();
    }

    public void showbuttomwidow(View view) {
        poplayout = (LinearLayout) getLayoutInflater().inflate(R.layout.photo,null);
        popupWindow = new PopupWindow(poplayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //点击空白隐藏pop
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        //添加弹出、弹入动画
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        int[]  location = new int[2];
        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.LEFT|Gravity.BOTTOM, 0, -location[1]);
        //添加按键事件监听
        setClickListener(poplayout);
        //添加pop关闭事件，实现关闭时改变背景的透明度
        popupWindow.setOnDismissListener(new poponDismissListener());
        backgroundAlpha(0.5f);

        obtainPermission();//6.0以后需要获取权限
    }

    private void obtainPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            list = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.CAMERA);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (list.size()!=0) {
                requestPermissions(list.toArray(new String[list.size()]),CODE_PERMISSION);
            }
        }else {
            FLAG_PERMISSION = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.d("test", Arrays.toString(grantResults));
        for (int i=0; i<grantResults.length;i++) {
            if (grantResults[i] == -1) {
                FLAG_PERMISSION = false;
                break;
            }
        }
        FLAG_PERMISSION = true;
    }

    private void setClickListener(LinearLayout layout) {
        TextView album = (TextView) layout.findViewById(R.id.album);
        TextView take = (TextView) layout.findViewById(R.id.take);

        album.setOnClickListener(new pictureListener());
        take.setOnClickListener(new pictureListener());
    }
    class pictureListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (!FLAG_PERMISSION) {
                Toast.makeText(UserInfo.this,"请先获取权限",Toast.LENGTH_LONG).show();
            }
            popupWindow.dismiss();
            switch (v.getId())  {
                case R.id.album:
                    Intent intent5 = new Intent(Intent.ACTION_PICK,null);
                    intent5.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                    startActivityForResult(intent5, CODE_ALBUM);
                    break;
                case R.id.take:
                    openCamera();
                    break;
            }
        }
    }

    private void openCamera() {
        Intent intent6 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),"head.jpg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraUri = FileProvider.getUriForFile(UserInfo.this,"com.air.airspeed.fileprovider",file);
        }else {
            cameraUri = Uri.fromFile(file);
        }
        intent6.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent6, CODE_CAMERA);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode)  {
            case CODE_ALBUM:
                if (resultCode == RESULT_OK) {
                    cropPhoto(data.getData());
                }
                break;
            case CODE_CAMERA:
                if (resultCode == RESULT_OK) {
                    //File temp = new File(Environment.getExternalStorageDirectory()+"/head.jpg");
                    cropPhoto(cameraUri);
                }
                break;
            case CODE_CROP:
                /*if (data != null) {
                    Bundle extras = data.getExtras();
                    head = extras.getParcelable("data");
                    if (head != null) {
                        //上传服务器
                        //setPicToView(head);
                        try {
                            headpic.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),cropUri));
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }*/

                try {
                    headpic.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(),cropUri));
                    saveheadpic(MediaStore.Images.Media.getBitmap(getContentResolver(),cropUri));

                }catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

    }

    public void saveheadpic(Bitmap bitmap) {
        //第一步：将Bitmap压缩至字节数组输出流ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,80,byteArrayOutputStream);
        //第二步：利用Base64将字节数组输出流中的数据转换成字符串String
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String imageString = new String(Base64.encodeToString(byteArray,Base64.DEFAULT));
        //第三步：将String保存到NetApp和SharedPreferences
        User user = NetApp.mInstance.getUser();
        user.setHeadPic(imageString);

        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = getSharedPreferences("UserInfo",MODE_PRIVATE);
        editor = pref.edit();
        editor.putString("HeadPic",imageString);
        editor.commit();

        //上传到服务器
        upLoadheadpic(user);
    }

    private void upLoadheadpic(User user) {
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("UserName", user.getUserName());
        mFormBodyBuild.add("Password", user.getPassWord());
        mFormBodyBuild.add("MemberDueDate",user.getDueDate());
        mFormBodyBuild.add("Credits",user.getVersion());
        mFormBodyBuild.add("Version",user.getVersion());
        mFormBodyBuild.add("HeadPic",user.getHeadPic());

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/UpLoadHeadServlet")
                .post(mFormBody)
                .build();HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
            }
            @Override
            public void onFailure(Call call, IOException e) {
            }

        });
    }

    public void cropPhoto(Uri uri) {
        Log.d("test","uri:"+uri.toString());
        File file = new File(Environment.getExternalStorageDirectory(),"cropImage.jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent7 = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent7.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        cropUri = Uri.fromFile(file);
        //找到制定uri对应的资源图片
        intent7.setDataAndType(uri,"image/*");
        intent7.putExtra("crop","true"); //可裁剪
        //aspectX,Y 是宽高的比例
        intent7.putExtra("aspectX",1);
        intent7.putExtra("aspectY",1);
        //outputX,Y 是裁剪图片宽高
        intent7.putExtra("outputX",150);
        intent7.putExtra("outputY",150);
        //intent7.putExtra("scale",true); 支持缩放
        intent7.putExtra("return-data",false);
        intent7.putExtra(MediaStore.EXTRA_OUTPUT,cropUri);
        intent7.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString()); //输出图片格式
        intent7.putExtra("noFaceDetection",true); //取消人脸识别
        //进入系统裁剪图片的界面
        startActivityForResult(intent7, CODE_CROP);

    }

    private void setPicToView(Bitmap mBitmap) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {  //检测sd卡是否可用
            return;
        }

        FileOutputStream b = null;
        File file = new File(path);
        boolean ex = file.mkdir();//创建以此File对象为名（path）的文件夹

        String fileName = path + "head.jpg"; //图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG,100,b);//把数据写入文件
        }catch (FileNotFoundException e) {
            Log.d("album","no such file or directory");
            e.printStackTrace();

        }finally {
            try {
                b.flush();
                b.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
