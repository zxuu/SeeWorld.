package com.iflytek.Test;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.mscv5plusdemo.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class PythonTTest extends Activity implements View.OnClickListener{
    private EditText result;

    //定义一个ServerSocket监听在端口8899上
    private Handler handler = null;
    String recv_buff = null;
    ServerSocket server = null;
    Socket socket = null;
    int port = 7643;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_python_ttest);
        initview();
        handler = new Handler();
        Toast.makeText(this, "haha", Toast.LENGTH_SHORT).show();
        try {
            server = new ServerSocket(7643);
        } catch (IOException e) {
            e.printStackTrace();
        }


//            try {
//                socket = server.accept();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (server != null) {
                        while (true) {
                            try {
                                socket = server.accept();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                handleResult();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        //Toast.makeText(PythonTTest.this, "server null", Toast.LENGTH_SHORT).show();
                    }
//                    while (true) {
//                        try {
//                            socket = server.accept();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            handleResult();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    try {
//                        socket = server.accept();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        handleResult();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }).start();
//            new Thread(new Task(socket)).start();


    }

    private void handleResult() throws Exception{
//        ObjectInputStream br = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//        ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//        HashMap map = null;
//        try {
//            map = (HashMap) br.readObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        String one = (String)map.get("1");
//        Toast.makeText(this, "hahahah" + one, Toast.LENGTH_SHORT).show();
//        br.close();
//        socket.close();

        //单开一个线程循环接收来自服务器端的消息
//        InputStream inputStream = null;
//        try {
//            inputStream = socket.getInputStream();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (inputStream!=null){
//            try {
//                byte[] buffer = new byte[1024];
//                int count = inputStream.read(buffer);//count是传输的字节数
//                recv_buff = new String(buffer);//socket通信传输的是byte类型，需要转为String类型
//                Toast.makeText(PythonTTest.this, "hahahah", Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    recv_buff = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(this, "hahahahahhah", Toast.LENGTH_LONG).show();
            }
        }).start();
        Toast.makeText(this, "hahahahahhah", Toast.LENGTH_LONG).show();
//        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        recv_buff = in.readLine();
//        Toast.makeText(this, "hahahahahhah", Toast.LENGTH_LONG).show();
    }

    private void initview() {
        findViewById(R.id.btn_start).setOnClickListener(this);
        result = (EditText) findViewById(R.id.edit_result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                start();
                break;
        }
    }

    private void start() {

    }

    public static class Task implements Runnable {

        public Socket socket;
        private Handler handler = null;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                handleSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void handleSocket() throws Exception {
//            ObjectInputStream br = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//            ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//            HashMap map = (HashMap) br.readObject();
//            String one = (String)map.get("1");
//
//            br.close();
//            socket.close();
        }
    }

    //不能在子线程中刷新UI，应为textView是主线程建立的
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            result.setText("haha");
        }
    };

}
