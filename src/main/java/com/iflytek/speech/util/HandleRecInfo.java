package com.iflytek.speech.util;

import com.alibaba.fastjson.JSON;
import com.iflytek.mscv5plusdemo.ReceiveBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HandleRecInfo {
    JSONObject jsonObject;
    JSONObject jsonObject2;
    String recv_buff = null;
    int msg;
    String thing = null;
    String[] things;
    Map<String,String> map = new HashMap<>();
    public HandleRecInfo(Socket socket) {
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream!=null){
            try {
                byte[] buffer = new byte[10024];
                int count = inputStream.read(buffer);//count是传输的字节数
                recv_buff = new String(buffer);//socket通信传输的是byte类型，需要转为String类型
                jsonObject = new JSONObject(recv_buff);
                msg = (int) jsonObject.get("1");
                thing = (String) jsonObject.get("2");
                //Toast.makeText(this, (String) jsonObject.get("1"), Toast.LENGTH_SHORT).show();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void handleInfo(ReceiveBack receiveBack) {
        receiveBack.receiveSelect(msg);
    }

    public String getThing() {
        if (null != thing) {
            return thing;
        }
        return "物体为空";
    }

    public String getThings() {
        map = (Map) JSON.parse(recv_buff);

//        Iterator it = jsonObject.keys();
//        int i = 0;
        int length = 0;
//        while (it.hasNext()) {
//            length++;
//        }
//        things = new String[length-1];
//        for (int j = 0; j < length; j++) {
//            try {
//                things[j] = (String) jsonObject.get(j + 2 + "");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        String d = "";
        length = map.size()-1;
//        things = new String[length];
//        for (int i = 2; i < map.size(); i++) {
//            things[i-2] = map.get(i + "");
//        }

        for (int i = 0; i < length; i++) {
            d = d + map.get(i + 2 + "") +",";
        }

//        for (int j = 0; j < things.length; j++) {
//            if (j == things.length) {
//                d = d + things[j];
//            } else
//                d = d + things[j] + ",";
//        }

        return d;
    }
}
