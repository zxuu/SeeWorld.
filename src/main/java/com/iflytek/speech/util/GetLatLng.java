package com.iflytek.speech.util;

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

public class GetLatLng {
    String url = "http://api.map.baidu.com/geocoder/v2/?address=中北大学&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";
    String recv_buff = null;
    URL myURL = null;
    URLConnection httpsConn = null;
    InputStream inputStream = null;
    double pt2lat = 0;
    double pt2lng = 0;
    String adrr;

    public GetLatLng(String adrr) {
        this.adrr = adrr;

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    public double getPt2lat() {
        return pt2lat;
    }

    public double getPt2lng() {
        return pt2lng;
    }

    public Map<String, BigDecimal> getLatAndLngByAddress(String addr){
        String address = "";
        String lat = "";
        String lng = "";
        try {
            address = java.net.URLEncoder.encode(addr,"UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String url = "http://api.map.baidu.com/geocoder/v2/?address=中北大学&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";

        URL myURL = null;
        URLConnection httpsConn = null;
        //进行转码
        try {
            myURL = new URL(url);
        } catch (MalformedURLException e) {

        }
        try {
            httpsConn = (URLConnection) myURL.openConnection();
            if (httpsConn != null) {
                InputStreamReader insr = new InputStreamReader(
                        httpsConn.getInputStream(), "UTF-8");
                BufferedReader br = new BufferedReader(insr);
                String data = null;
                if ((data = br.readLine()) != null) {
                    lat = data.substring(data.indexOf("\"lat\":")
                            + ("\"lat\":").length(), data.indexOf("},\"precise\""));
                    lng = data.substring(data.indexOf("\"lng\":")
                            + ("\"lng\":").length(), data.indexOf(",\"lat\""));
                }
                insr.close();
            }
        } catch (IOException e) {

        }
        Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
        map.put("lat", new BigDecimal(lat));
        map.put("lng", new BigDecimal(lng));
        return map;
    }

}
