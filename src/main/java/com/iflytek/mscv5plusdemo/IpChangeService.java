package com.iflytek.mscv5plusdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.util.GetPiIputil;

import java.net.URISyntaxException;

import static com.baidu.mapapi.BMapManager.getContext;

public class IpChangeService extends Service {
    String oldIpAdress;
    String newIpAdress;
    // 语音合成对象
    public SpeechSynthesizer mTts;
    WakeDemoActivity ipChange;
    Intent intent;
//    public MyBinder myBinder = new MyBinder();

//    public IpChangeService(String oldIpAdress,IpChange ipChange) {
//        this.oldIpAdress = oldIpAdress;
//        this.ipChange = ipChange;
//        mTts = ((SpeechApp) getApplication()).mTts;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            intent = Intent.getIntentOld("bindIntent");
            oldIpAdress = intent.getStringExtra("oldipadre");
            ipChange = (WakeDemoActivity) intent.getSerializableExtra("wakedemo");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (!oldIpAdress.equals(GetPiIputil.getIPAdress(getContext()))) {
                        oldIpAdress = GetPiIputil.getIPAdress(getContext());
//                        ipChange.getNewIp(oldIpAdress);
                        //int code = mTts.startSpeaking("您的IP地址发生了变化，请稍等", mTtsListener);
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    public class MyBinder extends Binder {
//        public void moniterIpChange(IpChange ipChange) {
//            ipChange.getNewIp(oldIpAdress);
//        }
//    }

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
//                showTip("播放完成");
            } else if (error != null) {
//                showTip(error.getPlainDescription(true));
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
