package com.speedtest.speedtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    private EditText packnumber;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setClick();
    }

    public void initView() {

        packnumber = (EditText)findViewById(R.id.packnumber);
        test = (Button) findViewById(R.id.test1);
        //test2 = (Button) findViewById(R.id.test2);
        //test3 = (Button) findViewById(R.id.test3);

        test.setText("开始测试");
    }

    public void setClick() {
        test.setOnClickListener(new ConfigClick());
        //test2.setOnClickListener(new ConfigClick());
        //test3.setOnClickListener(new ConfigClick());
    }

    class ConfigClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.test1:

                    if (test.getText().toString().equals("开始测试")){
                        test.setText("正在测试");
                        int packages1 = Integer.parseInt(packnumber.getText().toString());
                        test1(packages1);
                    }else if (test.getText().equals("测试结束")){
                        test.setText("开始测试");
                    }

                    break;
                /*case R.id.test2:
                    //int package2 = Integer.parseInt(packnumber.getText().toString());
                    //test2(package2);
                    break;
                case R.id.test3:
                    //int package3 = Integer.parseInt(packnumber.getText().toString());
                    //test3(package3);
                    break;*/
                default:
                    break;
            }
        }
    }

    private void test1(int number){
        String url = "http://p22n940119.iok.la/AirSpeed/TestServlet";

        for (int i=0;i<number;i++){
            sendtest(url,i);
            try {
                sleep(50);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        test.setText("测试结束");
    }

    private void sendtest(String url,int no){
        String text = "MIIEqDCCA5CgAwIBAgIJALOZgIbQVs/6MA0GCSqGSIb3DQEBBAUAMIGUMQswCQYD\n" +
                "VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4g\n" +
                "VmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UE\n" +
                "AxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAe\n" +
                "Fw0wODA0MTUyMjQwNTBaFw0zNTA5MDEyMjQwNTBaMIGUMQswCQYDVQQGEwJVUzET\n" +
                "MBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4G\n" +
                "A1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9p\n" +
                "ZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZI\n" +
                "hvcNAQEBBQADggENADCCAQgCggEBAJx4BZKsDV04HN6qZezIpgBuNkgMbXIHsSAR\n" +
                "vlCGOqvitV0Amt9xRtbyICKAx81Ne9smJDuKgGwms0sTdSOkkmgiSQTcAUk+fArP\n" +
                "GgXIdPabA3tgMJ2QdNJCgOFrrSqHNDYZUer3KkgtCbIEsYdeEqyYwap3PWgAuer9\n" +
                "5W1Yvtjo2hb5o2AJnDeoNKbf7be2tEoEngeiafzPLFSW8s821k35CjuNjzSjuqtM\n" +
                "9TNxqydxmzulh1StDFP8FOHbRdUeI0+76TybpO35zlQmE1DsU1YHv2mi/0qgfbX3\n" +
                "6iANCabBtJ4hQC+J7RGQiTqrWpGA8VLoL4WkV1PPX8GQccXuyCcCAQOjgfwwgfkw\n" +
                "HQYDVR0OBBYEFE/koLPdnLop9x1yh8Tnw48ghsKZMIHJBgNVHSMEgcEwgb6AFE/k\n" +
                "oLPdnLop9x1yh8Tnw48ghsKZo";
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("No", String.valueOf(no));
        mFormBodyBuild.add("text", text);

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url(url)
                .post(mFormBody)
                .build();
        //获取当前时间
        java.util.Date systemtime = new java.util.Date();
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        Log.d("sendtest", "time is "+ s.format(systemtime) +" No. is " +String.valueOf(no));

        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.d("receivetest", "already send and receive response");
                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                    String seq = jsonObject.getString("No");
                    java.util.Date systemtime = new java.util.Date();
                    java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    Log.d("receivetest","time is "+s.format(systemtime)+" No. is "+seq);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("receivetestfail", Log.getStackTraceString(e));
            }
        });
    }

}
