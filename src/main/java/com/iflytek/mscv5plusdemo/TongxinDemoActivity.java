package com.iflytek.mscv5plusdemo;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.util.HandleRecInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TongxinDemoActivity extends Activity implements View.OnClickListener{
    private EditText et_send;
    private Button bt_send;
    private TextView tv_recv;
    private String send_buff=null;
    private String recv_buff=null;
    private Handler handler = null;
    Socket socket = null;

    // 语音合成对象
    private SpeechSynthesizer mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tongxin_demo2);

        initView();

        handler = new Handler();
        //单开一个线程来进行socket通信
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket("192.168.42.233" , 7654);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket!=null) {
                        System.out.println("###################");
                        while (true) {      //循环进行收发
                            recv();
                        }
                    } else
                        System.out.println("socket is null");
            }
        }).start();
//        send();
//        mySend();
    }

    //不能在子线程中刷新UI，应为textView是主线程建立的
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            tv_recv.append("\n"+recv_buff);
        }
    };

    private void send() {
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send_buff = et_send.getText().toString();
                        //向服务器端发送消息
                        System.out.println("------------------------");
                        OutputStream outputStream=null;
                        try {
                            outputStream = socket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if(outputStream!=null){
                            try {
                                outputStream.write(send_buff.getBytes());
                                System.out.println("1111111111111111111111");
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();

            }
        });
    }

    private void initView() {
        et_send = (EditText) findViewById(R.id.et_send2);
        findViewById(R.id.bt_send2).setOnClickListener(this);
        tv_recv = (TextView) findViewById(R.id.tv_recv2);
        mTts = ((SpeechApp) getApplication()).mTts;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send2:
                mySend();
                break;
        }
    }

    private void mySend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send_buff = et_send.getText().toString();
                //向服务器端发送消息
//                System.out.println("------------------------");
                OutputStream outputStream=null;
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(outputStream!=null){
                    try {
                        outputStream.write(send_buff.getBytes());
//                        System.out.println("1111111111111111111111");
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private void recv() {

//        //单开一个线程循环接收来自服务器端的消息
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
//                JSONObject jsonObject = new JSONObject(recv_buff);
//                System.out.println(jsonObject.get("1"));
//                recv_buff = (String) jsonObject.get("1");
//                //Toast.makeText(this, (String) jsonObject.get("1"), Toast.LENGTH_SHORT).show();
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        //将受到的数据显示在TextView上
////        if (recv_buff!=null){
////            handler.post(runnableUi);
////
////        }
        HandleRecInfo handleRecInfo = new HandleRecInfo(socket);
        handleRecInfo.handleInfo(new ReceiveBack() {
            @Override
            public void receiveSelect(int msg) {
                switch (msg) {
                    case 1: //小飞小飞，我的位置在哪儿
                        recv_buff = msg + "";
                        int code = mTts.startSpeaking("您的位置再中北大学附近", mTtsListener);
                        break;
                    case 2: //小飞小飞，导航去太原理工大学

                        break;
                    case 3: //小飞小飞。我的正前方有哪些东西

                        break;
                    case 4: //小飞小飞，我指示得东西是什么

                        break;
                    case 5: //小飞小飞，现在是红灯还是绿灯

                        break;
                    case 6: //小飞小飞，现在几点了

                        break;
                    case 7:

                        break;
                }
            }
        });

        //将受到的数据显示在TextView上
        if (recv_buff!=null){
            handler.post(
                    runnableUi);

        }
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
//			mPercentForBuffering = percent;
//			showTip(String.format(getString(R.string.tts_toast_format),
//					mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
//			mPercentForPlaying = percent;
//			showTip(String.format(getString(R.string.tts_toast_format),
//					mPercentForBuffering, mPercentForPlaying));
//
//			SpannableStringBuilder style=new SpannableStringBuilder(texts);
//			Log.e(TAG,"beginPos = "+beginPos +"  endPos = "+endPos);
//			if(!"henry".equals(voicerCloud)||!"xiaoyan".equals(voicerCloud)||
//					!"xiaoyu".equals(voicerCloud)||!"catherine".equals(voicerCloud))
//				endPos++;
//			style.setSpan(new BackgroundColorSpan(Color.RED),beginPos,endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			((EditText) findViewById(R.id.tts_text)).setText(style);
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Toast.makeText(TongxinDemoActivity.this, "完成", Toast.LENGTH_SHORT).show();
            } else if (error != null) {
                Toast.makeText(TongxinDemoActivity.this, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

}
