package com.iflytek.speech.util;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
public class adresstolatlan {

    public static String loadJSON (String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    yc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
        }
        return json.toString();
    }

    public static String getData(URL url) {
        BufferedReader bufferedReader;
        StringBuffer stringBuffer = new StringBuffer();
        try {
//            URL url = new URL("http://cache.video.iqiyi.com/jp/avlist/202861101/1/?callback=jsonp9");//json地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");//使用get方法接收
            InputStream inputStream = connection.getInputStream();//得到一个输入流
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTf-8"));
            String sread = null;
            while ((sread = bufferedReader.readLine()) != null) {
                stringBuffer.append(sread + "\n");
//                stringBuffer.append("\r\n");
            }
//            Log.i("msg", "onClick: " + stringBuffer.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    public static Map<String, Double> getLatAndLngByAddress(String addr){
        String address = "";
        String lat = "";
        String lng = "";
        try {
            address = java.net.URLEncoder.encode(addr,"UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
//        String url = String.format("http://api.map.baidu.com/geocoder/v2/?"
//                +"ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&output=json&address=%s",address);
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
        Map<String, Double> map = new HashMap<String, Double>();
//        map.put("lat", Double.valueOf(lat));
//        map.put("lng", Double.valueOf(lng));
        return map;
    }

    public static Map<String, Double> adrtolatlng() {
        String url = "http://api.map.baidu.com/geocoder/v2/?address=中北大学&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";
//        Map<String, Double> map = new HashMap<>();
        String recv_buff = null;
        URL myURL = null;
        URLConnection httpsConn = null;
        InputStream inputStream = null;
        Map<String, Double> map = new HashMap<>();
        Double lng = 0.0;
        Double lat = 0.0;
        //进行转码
        try {
            myURL = new URL(url);
            httpsConn = (URLConnection) myURL.openConnection();

            inputStream = httpsConn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream!=null){
            try {
                byte[] buffer = new byte[1024];
                int count = inputStream.read(buffer);//count是传输的字节数
                recv_buff = new String(buffer);//socket通信传输的是byte类型，需要转为String类型
                JSONObject jsonObject = new JSONObject(recv_buff);

                lng= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lng");
                lat= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lat");
                map.put("lng", lng);
                map.put("lat", lat);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
