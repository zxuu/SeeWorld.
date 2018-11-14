package com.iflytek.Test;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.mscv5plusdemo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class test extends Activity implements View.OnClickListener{

    EditText editText;

    String url = "http://api.map.baidu.com/geocoder/v2/?address=中北大学&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";
    String recv_buff = null;
    URL myURL = null;
    URLConnection httpsConn = null;
    InputStream inputStream = null;
    double pt2lat = 8.0;
    double pt2lng = 0.0;
    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        handler = new Handler();
        findViewById(R.id.st).setOnClickListener(this);
        editText = (EditText) findViewById(R.id.ed);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.st:
//
                df();
                break;
        }
    }

    private void df() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    myURL = new URL(url);
                    httpsConn = (URLConnection) myURL.openConnection();
                    inputStream = httpsConn.getInputStream();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (inputStream!=null){
                    try {
                        byte[] buffer = new byte[1024];
                        int count = inputStream.read(buffer);//count是传输的字节数
                        recv_buff = new String(buffer);//socket通信传输的是byte类型，需要转为String类型
                        JSONObject jsonObject = new JSONObject(recv_buff);

                        pt2lng= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lng");
                        pt2lat= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lat");
                        handler.post(runnableUi1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else
                    handler.post(runnableUi2);
            }
        }).start();
    }

    //不能在子线程中刷新UI，应为textView是主线程建立的
    Runnable runnableUi1 = new Runnable() {
        @Override
        public void run() {
            editText.append("\n"+String.valueOf(pt2lng) + String.valueOf(pt2lat));
        }
    };

    //不能在子线程中刷新UI，应为textView是主线程建立的
    Runnable runnableUi2 = new Runnable() {
        @Override
        public void run() {
            editText.append("\n"+"空");
        }
    };
}
