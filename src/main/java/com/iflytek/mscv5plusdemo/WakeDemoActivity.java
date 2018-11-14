package com.iflytek.mscv5plusdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.speech.setting.LocationService;
import com.iflytek.speech.util.GetPiIputil;
import com.iflytek.speech.util.GetTime;
import com.iflytek.speech.util.HandleRecInfo;
import com.iflytek.speech.util.HandleSend;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import com.iflytek.aiui.AIUIAgent;

import static com.baidu.mapapi.BMapManager.getContext;

public class WakeDemoActivity extends Activity implements View.OnClickListener {
	private String TAG = "ivw";
	private Toast mToast;
	private EditText editText;
	private EditText result;
	BootCompleteReceiver mOnepxReceiver;
	// 语音唤醒对象
	private VoiceWakeuper mIvw;
    /////////////////////////////////
    // 语音听写对象
    private SpeechRecognizer mIat;
    private Timer timer;
    //    private boolean mTranslateEnable = false;
    ///////////////////////导航////////////////////////////
    private LocationService locationService;
    private double lat; private double lng;
    private BDLocation MyBdLocation;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    private EditText et_send;
    private Button bt_send;
    private TextView tv_recv;
    private String send_buff=null;
    private String recv_buff=null;
    private Handler handler = null;
//    Socket socket = null;
    //------------------------------
    double ptlat=0;
    double ptlng=0;
    //------------------------------
    String myAdrres;
    String piIP;
    String url = null;
    String recv_buff2 = null;
    String destination = null;
    URL myURL = null;
    URLConnection httpsConn = null;
    InputStream inputStream = null;
    HandleSend handleSend; //指令帮助类
    double pt2lat = 8.0;
    double pt2lng = 0.0;
    private Thread socketConnect;
    //------------AIUI--------------
    private AIUIAgent mAIUIAgent = null;
    //交互状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //setSocket();
        //((SpeechApp) getApplication()).socket = (Socket) savedInstanceState.get("socket");
            Log.d(TAG, ".......................onCreate:................................");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setStatusBarColor(this.getResources().getColor(R.color.title_color));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.test);

		requestPermissions();
		init();
		initmap();
        handler = new Handler();
        //------------------------------
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if(mIvw != null) {
            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
        } else {
            showTip("唤醒未初始化");
        }
        //--------------------------------
        //单开一个线程来进行socket通信
//        new Thread(new Runnable() {
//            @Override
//        public void run() {
//
//            if (((SpeechApp) getApplication()).socket!=null) {
//                Log.d(TAG, "############################");
//                while (true) {      //循环进行收发
//                    recv();
//                }
//            } else
//                Log.d(TAG, "socket is null");
//        }
//        }).start();

        //单开一个线程查看IP地址是否变化
        monitorIPThread();
	}

    private void initmap() {
        // -----------location config ------------
        locationService = ((SpeechApp) getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();// 定位SDK
//        startLocation.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (startLocation.getText().toString().equals(getString(R.string.startlocation))) {
//                    locationService.start();// 定位SDK
//                    // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
//                    startLocation.setText(getString(R.string.stoplocation));
//                } else {
//                    locationService.stop();
//                    startLocation.setText(getString(R.string.startlocation));
//                }
//            }
//        });
    }

    private void init() {
        //注册监听屏幕的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BootCompleteReceiver(),filter);
        //------------------------------------------------------------
        mIat = ((SpeechApp) getApplication()).mIat;
        // 初始化唤醒对象
        mIvw = ((SpeechApp) getApplication()).mIvw;
        //语音合成对象
        mTts = ((SpeechApp) getApplication()).mTts;
//		mIvw = VoiceWakeuper.createWakeuper(this, null);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        findViewById(R.id.btn_weizhi).setOnClickListener(this);
        findViewById(R.id.btn_daohang).setOnClickListener(this);
        findViewById(R.id.btn_zhishi).setOnClickListener(this);
        findViewById(R.id.btn_qianfang).setOnClickListener(this);
        findViewById(R.id.btn_hldong).setOnClickListener(this);
        findViewById(R.id.btn_shijian).setOnClickListener(this);
//        findViewById(R.id.btn_tongxin).setOnClickListener(this);


//        et_send = (EditText) findViewById(R.id.et_send);



        myAdrres = GetPiIputil.getIPAdress(getContext());
    }

    int ret = 0;
    @Override
	public void onClick(View v) {
		switch (v.getId()) {
            case R.id.btn_daohang:
                destination = "北京天安门广场";
                daoHang();
                break;
            case R.id.btn_weizhi:
                weizhi();
                break;
            case R.id.btn_zhishi:
                if (((SpeechApp) getApplication()).socket == null) {
                    mTts.startSpeaking("请正确连接树莓派", mTtsListener);
                } else {
                    mySend(4);
                }
//                mySend(4);
                break;
            case R.id.btn_qianfang:
                if (((SpeechApp) getApplication()).socket == null) {
                    mTts.startSpeaking("请正确连接树莓派", mTtsListener);
                } else {
                    mySend(3);
                }
//                mySend(3);
                break;
            case R.id.btn_hldong:
                if (((SpeechApp) getApplication()).socket == null) {
                    mTts.startSpeaking("请正确连接树莓派", mTtsListener);
                } else {
                    mySend(5);
                }
//                mySend(5);
                break;
            case R.id.btn_shijian:
                int code = mTts.startSpeaking(GetTime.getTime(), mTtsListener);
                //mIat.stopListening();

//                String baike = "百科一下张学友";
//                if (!checkAIUIAgent()) {
//                    return;
//                }
//                startVoiceNlp(baike);
//                mIvw.startListening(mWakeuperListener);
                break;
//            case R.id.btn_tongxin:
//                startActivity(new Intent(WakeDemoActivity.this,GetPiIP.class));
//                break;
//            case R.id.bt_send:
//                startActivity(new Intent(this, WalkRouteCalculateActivity.class));
//                mySend(4);
//                startActivity(new Intent(this, com.iflytek.Test.Button.class));

		default:
			break;
		}		
	}

    private void printResult(RecognizerResult results) {

        handleSend = new HandleSend(results);
        handleSend.handleSendInfo(new SendCallBack() {
            @Override
            public void handleSend(int Msg) {
                switch (Msg) {
                    case 1: //小飞小飞，我的位置在哪儿
                        weizhi();
                        break;
                    case 2: //小飞小飞，导航去***
                        destination = handleSend.getDestination();
                        daoHang();
                        break;
                    case 3: //小飞小飞，我的正前方有哪些东西
                        //mySend(3);
                        timer=new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Message message=new Message();
                                message.what=0;
                                mHandler.sendMessage(message);
                            }
                        },1000);
                        break;
                    case 4: //小飞小飞，我指示的东西是什么
                        mySend(4);
                        break;
                    case 5: //小飞小飞，现在是红灯还是绿灯
                        mySend(5);
                        break;
                    case 6: //小飞小飞，现在几点了
                        int code = mTts.startSpeaking(GetTime.getTime(), mTtsListener);
                        break;
                    case 7:
//                        PackageManager packageManager = getPackageManager();
//                        Intent intent = new Intent();
//                        intent = packageManager.getLaunchIntentForPackage("com.iflytek.mscv5plusdemo");
//                        if (intent == null) {
//                            int code7 = mTts.startSpeaking("未安装", mTtsListener);
//                        } else
//                            startActivity(intent);
//                        mIat.stopListening();
//                        mIvw.stopListening();
                        String baike = handleSend.getBaikeYiXia();
                        if (!checkAIUIAgent()) {
                            showTip("notAIUI");
                        } else {
                            showTip("AIUIstart");
                            startVoiceNlp(baike);
                        }
//                        mIvw.startListening(mWakeuperListener);
                        break;
                    case 8:
                        mTts.stopSpeaking();
                        break;
                    case 10:
                        showTip("停止导航");
                        ((SpeechApp) getApplication()).destoryActivity("WalkRouteCalculateActivity");
                        mTts.startSpeaking("结束导航成功", mTtsListener);
                        break;
                    case 11:
                        String tianqi = handleSend.getBaikeYiXia();
                        if (!checkAIUIAgent()) {
                            showTip("notAIUI");
                        } else {
                            showTip("AIUIstart");
                            startVoiceNlp(tianqi);
                        }
                        break;
                    default:

                        break;
                }
            }
        });
    }

    private void recv() {
        final HandleRecInfo handleRecInfo = new HandleRecInfo(((SpeechApp)getApplication()).socket);
        handleRecInfo.handleInfo(new ReceiveBack() {
            @Override
            public void receiveSelect(int msg) {
                switch (msg) {
                    case 1: //回答(小飞小飞，我的位置在哪儿)
//                        recv_buff = msg + "";
                        //此功能在手机端完成，不必接收树莓派的信息
                        break;
                    case 2: //回答(小飞小飞，导航去***)
                        //此功能在手机端完成，不必接收树莓派的信息
                        break;
                    case 3: //回答(小飞小飞，我的正前方有哪些东西)
                        String thingsStr = handleRecInfo.getThings();
                        recv_buff = msg + thingsStr;
                        showTip(recv_buff + "正前方");
                        int code3 = mTts.startSpeaking(thingsStr, mTtsListener);
                        break;
                    case 4: //回答(小飞小飞，我指示的东西是什么)
                        recv_buff = msg + "";
                        showTip(recv_buff + "指示");

                        int code4 = mTts.startSpeaking(handleRecInfo.getThing(), mTtsListener);
                        break;
                    case 5: //回答(小飞小飞，现在是红灯还是绿灯)
                        recv_buff = msg + "";
                        int code5 = mTts.startSpeaking(handleRecInfo.getThing(), mTtsListener);
                        break;
                    case 6: //回答(小飞小飞，现在几点了)
                        //此功能在手机端完成，不必接收树莓派的信息
                        break;
                    case 7:

                        break;
                }
            }
        });

        //将收到的数据显示在TextView上
//        if (recv_buff!=null){
//            handler.post(runnableUi);
//
//        }
    }

    private void mySend(final int code) {
//        socketConnect = new Thread(new SocketConnect());
//        socketConnect.start();

        new Thread(new Runnable() {
            @Override
            public void run() {

                OutputStream outputStream=null;
                try {
                    outputStream = ((SpeechApp) getApplication()).socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(outputStream!=null){
                    try {
                        outputStream.write(String.valueOf(code).getBytes());
//                        System.out.println("1111111111111111111111");
                        outputStream.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
//			showTip("唤醒成功");
//			editText.setText("小飞小飞在");
            mIvw.stopListening();
			mTts.startSpeaking("小飞在", mTtsListener);
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip("请开始说话");
            }
		}

		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
			switch( eventType ){
			// EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
			case SpeechEvent.EVENT_RECORD_DATA:
				final byte[] audio = obj.getByteArray( SpeechEvent.KEY_EVENT_RECORD_DATA );
				Log.i( TAG, "ivw audio length: "+audio.length );
				break;
			}
		}

		@Override
		public void onVolumeChanged(int volume) {
//			showTip("当前音量："+volume);
		}
	};


	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            if(false && error.getErrorCode() == 14002) {
                showTip( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
            } else {
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            mIvw.startListening(mWakeuperListener);
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            printResult(results);

            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            Log.d(TAG, "返回音频数据："+data.length);
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

//    private void printResult(RecognizerResult results) {
////
////        handleSend = new HandleSend(results);
////        handleSend.handleSendInfo(new SendCallBack() {
////            @Override
////            public void handleSend(int Msg) {
////                switch (Msg) {
////                    case 1: //小飞小飞，我的位置在哪儿
////                        weizhi();
////                        break;
////                    case 2: //小飞小飞，导航去***
////                        destination = handleSend.getDestination();
////                        daoHang();
////                        break;
////                    case 3: //小飞小飞，我的正前方有哪些东西
////                        mySend(3);
////                        break;
////                    case 4: //小飞小飞，我指示的东西是什么
////                        mySend(4);
////                        break;
////                    case 5: //小飞小飞，现在是红灯还是绿灯
////                        mySend(5);
////                        break;
////                    case 6: //小飞小飞，现在几点了
////                        int code = mTts.startSpeaking(GetTime.getTime(), mTtsListener);
////                        break;
////                    case 7:
//////                        PackageManager packageManager = getPackageManager();
//////                        Intent intent = new Intent();
//////                        intent = packageManager.getLaunchIntentForPackage("com.iflytek.mscv5plusdemo");
//////                        if (intent == null) {
//////                            int code7 = mTts.startSpeaking("未安装", mTtsListener);
//////                        } else
//////                            startActivity(intent);
//////                        mIat.stopListening();
//////                        mIvw.stopListening();
////                        String baike = handleSend.getBaikeYiXia();
////                        if (!checkAIUIAgent()) {
////                            showTip("notAIUI");
////                        } else {
////                            showTip("AIUIstart");
////                            startVoiceNlp(baike);
////                        }
//////                        mIvw.startListening(mWakeuperListener);
////                        break;
////                    default:
////
////                        break;
////                }
////            }
////        });
////    }

    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void daoHang() {
        locationService.start();
        lat = MyBdLocation.getLatitude();
        lng = MyBdLocation.getLongitude();
        ((SpeechApp) getApplication()).lat = lat;
        ((SpeechApp) getApplication()).lng = lng;

        Thread daoHangThre = new Thread(new daoHangUrlThre());
        daoHangThre.start();
        try {
            daoHangThre.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        LatLng pt1 = new LatLng(lat,lng);//38.016107,112.449064
//        LatLng pt2 = new LatLng(pt2lat,pt2lng);
//        // 构建 导航参数
//        final NaviParaOption para = new NaviParaOption()
//                .startPoint(pt1).endPoint(pt2);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    BaiduMapNavigation.openBaiduMapWalkNavi(para, getContext());
//                } catch (BaiduMapAppNotSupportNaviException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        startActivity(new Intent(this,WalkRouteCalculateActivity.class));
    }

    //位置实例
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation loc) {
            // TODO Auto-generated method stub
            MyBdLocation = loc;
            if (null != MyBdLocation && MyBdLocation.getLocType() != BDLocation.TypeServerError) {
                StringBuffer sb = new StringBuffer(256);
//                sb.append("time : ");
//                location.getPoiList();
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
//                sb.append(location.getTime());
//                sb.append("\nlocType : ");// 定位类型
//                sb.append(location.getLocType());
//                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
//                sb.append(location.getLocTypeDescription());
//                sb.append("\nlatitude : ");// 纬度
//                sb.append(location.getLatitude());
                lat = MyBdLocation.getLatitude();
                lng = MyBdLocation.getLongitude();
//                sb.append("\nlontitude : ");// 经度
//                sb.append(location.getLongitude());
//                sb.append("\nradius : ");// 半径
//                sb.append(location.getRadius());
//                sb.append("\nCountryCode : ");// 国家码
//                sb.append(location.getCountryCode());
//                sb.append("\nCountry : ");// 国家名称
//                sb.append(location.getCountry());
//                sb.append("\ncitycode : ");// 城市编码
//                sb.append(location.getCityCode());
//                sb.append("\ncity : ");// 城市
//                sb.append(location.getCity());
//                sb.append("\nDistrict : ");// 区
//                sb.append(location.getDistrict());
//                sb.append("\nStreet : ");// 街道
//                sb.append(location.getStreet());
//                sb.append("\naddr : ");// 地址信息
//                sb.append(location.getAddrStr());
//                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
//                sb.append(location.getUserIndoorState());
//                sb.append("\nDirection(not all devices have value): ");
//                sb.append(location.getDirection());// 方向
//                sb.append("\nlocationdescribe: ");
//                sb.append(location.getLocationDescribe());// 位置语义化信息
//                sb.append("\nPoi: ");// POI信息
                if (MyBdLocation.getPoiList() != null && !MyBdLocation.getPoiList().isEmpty()) {
                    for (int i = 0; i < MyBdLocation.getPoiList().size(); i++) {
                        Poi poi = (Poi) MyBdLocation.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
//                    result.setText(sb.toString());
                }
//                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                    sb.append("\nspeed : ");
//                    sb.append(location.getSpeed());// 速度 单位：km/h
//                    sb.append("\nsatellite : ");
//                    sb.append(location.getSatelliteNumber());// 卫星数目
//                    sb.append("\nheight : ");
//                    sb.append(location.getAltitude());// 海拔高度 单位：米
//                    sb.append("\ngps status : ");
//                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
//                    sb.append("\ndescribe : ");
//                    sb.append("gps定位成功");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                    // 运营商信息
//                    if (location.hasAltitude()) {// *****如果有海拔高度*****
//                        sb.append("\nheight : ");
//                        sb.append(location.getAltitude());// 单位：米
//                    }
//                    sb.append("\noperationers : ");// 运营商信息
//                    sb.append(location.getOperators());
//                    sb.append("\ndescribe : ");
//                    sb.append("网络定位成功");
//                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                    sb.append("\ndescribe : ");
//                    sb.append("离线定位成功，离线定位结果也是有效的");
//                } else if (location.getLocType() == BDLocation.TypeServerError) {
//                    sb.append("\ndescribe : ");
//                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
//                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
//                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                    sb.append("\ndescribe : ");
//                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
//                }
//                logMsg(sb.toString());
            }
        }

    };

    private void weizhi() {
        StringBuffer sb = new StringBuffer(256);
        String locdescription;
        locationService.start();// 定位SDK
        if (MyBdLocation.getPoiList() != null && !MyBdLocation.getPoiList().isEmpty()) {
            for (int i = 0; i < MyBdLocation.getPoiList().size(); i++) {
                Poi poi = (Poi) MyBdLocation.getPoiList().get(i);
                sb.append(poi.getName() + ";");
            }
//            result.setText(sb.toString());
        }
        locdescription=MyBdLocation.getLocationDescribe();
//        mTts = ((SpeechApp) getApplication()).mTts;
        int code = mTts.startSpeaking(locdescription, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            showTip("语音合成失败,错误码: " + code);
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
                showTip("播放完成");
            } else if (error != null) {
                showTip(error.getPlainDescription(true));
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


    //不能在子线程中刷新UI，应为textView是主线程建立的
//    Runnable runnableUi = new Runnable() {
//        @Override
//        public void run() {
//            result.setText("\n"+recv_buff);
//        }
//    };
//
//    Runnable runnableUi2 = new Runnable() {
//        @Override
//        public void run() {
//            tv_recv.append("\n"+"ipchange");
//        }
//    };

    private class daoHangUrlThre implements Runnable {

        @Override
        public void run() {
            //String url = "http://api.map.baidu.com/geocoder/v2/?address=中北大学&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";
            url = "http://api.map.baidu.com/geocoder/v2/?address=" + destination + "&output=json&ak=GY8rADx7a5mi8qVU1Kz755c2GU05Arnw&mcode=51:E8:27:D4:BA:68:4F:98:D0:42:F8:92:31:22:EC:61:D1:EE:D7:8E;com.iflytek.mscv5plusdemo";
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
                    recv_buff2 = new String(buffer);//socket通信传输的是byte类型，需要转为String类型
                    JSONObject jsonObject = new JSONObject(recv_buff2);

                    ((SpeechApp) getApplication()).pt2lng= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lng");
                    ((SpeechApp) getApplication()).pt2lat= jsonObject.getJSONObject("result").getJSONObject("location").getDouble("lat");
//                        handler.post(runnableUi1);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SocketConnect implements Runnable {
        @Override
        public void run() {
//            try {
//                if (socket.getKeepAlive()) {
//                    socket.setKeepAlive(false);
//                    socket.close();
//                }
//            } catch (SocketException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                piIP = GetPiIputil.getConnectedIP().get(0);
//                socket = new Socket(piIP , 7654);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }


    private void monitorIPThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!myAdrres.equals(GetPiIputil.getIPAdress(getContext()))) {
                        myAdrres = GetPiIputil.getIPAdress(getContext());
//                        handler.post(runnableUi2);
                        int code = mTts.startSpeaking("您的ip地址发生了变化，请稍等", mTtsListener);
//                        if (code != ErrorCode.SUCCESS) {
//                            showTip("语音合成失败,错误码: " + code);
//                        }
                    }
                }
            }
        }).start();
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Log.d(TAG, "............................onSaveInstanceState:......................");
//        outState.putSerializable("socket", (Serializable) ((SpeechApp) getApplication()).socket);
//    }
    private void setSocket() {
        if (!((SpeechApp) getApplication()).socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((SpeechApp) getApplication()).socket = new Socket(GetPiIputil.getConnectedIP().get(0) , 7653);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
//        try {
//            if (!((SpeechApp) getApplication()).socket.getKeepAlive()) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            ((SpeechApp) getApplication()).socket = new Socket(GetPiIputil.getConnectedIP().get(0) , 7654);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
    }

    public boolean checkAIUIAgent() {
        if (null == mAIUIAgent) {
            Log.i(TAG, "create aiui agent");

            //创建AIUIAgent
            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
//            AIUIMessage startMsg = new AIUIMessage(AIUIConstant.CMD_START, 0, 0, null, null);
//            mAIUIAgent.sendMessage(startMsg);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建 AIUI Agent 失败！";
            showTip(strErrorTip);
        }

        return null != mAIUIAgent;
    }
    public String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];

            ins.read(buffer);
            ins.close();

            params = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return params;
    }
    //AIUI事件监听器
    public AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            switch (event.eventType) {
                case AIUIConstant.EVENT_WAKEUP:
                    //唤醒事件
                    Log.i(TAG, "on event: " + event.eventType);
                    showTip("进入识别状态");
                    break;

                case AIUIConstant.EVENT_RESULT: {
                    //结果事件
                    showTip("EVENT_RESULT");
                    try {
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            String sub = params.optString("sub");
                            JSONObject result = cntJson.optJSONObject("intent");
                            if ("nlp".equals(sub) && result.length() > 2) {
                                // 解析得到语义结果
                                String str = "";
                                //在线语义结果
                                if (result.optInt("rc") == 0) {
                                    JSONObject answer = result.optJSONObject("answer");
                                    if (answer != null) {
                                        str = answer.optString("text");
                                    }
                                } else {
                                    str = "rc4，无法识别";
                                }
                                if (!TextUtils.isEmpty(str)) {
//                                    showTip("张学友");
                                    mTts.startSpeaking(str, mTtsListener);
                                } else {
                                    showTip("空");
                                }

                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
//                        mNlpText.append("\n");
//                        mNlpText.append(e.getLocalizedMessage());
                    }

//                    mNlpText.append("\n");
                }
                break;

                case AIUIConstant.EVENT_ERROR: {
                    //错误事件
                    Log.i(TAG, "on event: " + event.eventType);
//                    mNlpText.append("\n");
//                    mNlpText.append("错误: " + event.arg1 + "\n" + event.info);
                }
                break;

                case AIUIConstant.EVENT_VAD: {
                    //vad事件
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        //找到语音前端点
                        showTip("找到vad_bos");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        //找到语音后端点
                        showTip("找到vad_eos");
                    } else {
                        showTip("" + event.arg2);
                    }
                }
                break;

                case AIUIConstant.EVENT_START_RECORD: {
                    //开始录音事件
                    Log.i(TAG, "on event: " + event.eventType);
                    showTip("开始录音");
                }
                break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    //停止录音事件
                    Log.i(TAG, "on event: " + event.eventType);
                    showTip("停止录音");
                }
                break;

                case AIUIConstant.EVENT_STATE: {    // 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        showTip("STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        showTip("STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        showTip("STATE_WORKING");
                    }
                }
                break;


                default:
                    break;
            }
        }

    };

    //开始录音
    public void startVoiceNlp(String callperson) {
        Log.i(TAG, "start voice nlp");

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        // 默认为oneshot 模式，即一次唤醒后就进入休眠，如果语音唤醒后，需要进行文本语义，请将改段逻辑copy至startTextNlp()开头处
        if (AIUIConstant.STATE_WORKING != this.mAIUIState) {
            showTip("先发送唤醒消息");
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音
        String params = "data_type=text";
        byte[] textData = callperson.getBytes();
        AIUIMessage writeMsg = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
        //showTip("打开AIUI内部录音机，开始录音");
        mAIUIAgent.sendMessage(writeMsg);
    }

    // (2) 使用handler处理接收到的消息
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                mySend(3);
            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "....................WakeDeno...................onStart:..........................");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "...................wakedemoonPause: ...............................");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "wakedemo....................onResume:.............................");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        if (!((SpeechApp) getApplication()).socket.isConnected()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        ((SpeechApp) getApplication()).socket = new Socket(GetPiIputil.getConnectedIP().get(0) , 7653);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }
        Log.d(TAG, "......................WakeDenoonRestart: restar.........................");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "........onStop: ...................WakeDeno......................");
//        if (((SpeechApp) getApplication()).socket.isConnected()) {
//            Toast.makeText(this, "socket is Connect", Toast.LENGTH_SHORT).show();
//        } else
//            Toast.makeText(this, "socket is not Connect", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "..................onDestroy WakeDemoActivity:onDestroy..................");
        if( null != mTts ){
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        // 销毁合成对象
//        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
            mIat.cancel();
            mIat.destroy();
        }

        mySend(9);

        try {
            ((SpeechApp) getApplication()).socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}