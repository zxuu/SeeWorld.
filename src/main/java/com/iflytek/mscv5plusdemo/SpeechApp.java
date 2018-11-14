package com.iflytek.mscv5plusdemo;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.speech.setting.LocationService;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.GetPiIputil;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class SpeechApp extends Application{
    public static Map<String,Activity> destroyMap = new HashMap<>();

	public LocationService locationService;
	public Vibrator mVibrator;
	////////////////Tts//////////////////////
	// 语音合成对象
	public SpeechSynthesizer mTts;
	// 语音唤醒对象
	public VoiceWakeuper mIvw;
	// 语音听写对象
	public SpeechRecognizer mIat;
	private boolean mTranslateEnable = false;

	public Socket socket;
	public String piIP;

	// 默认云端发音人
	public static String voicerCloud="xiaoyan";
	// 默认本地发音人
	public static String voicerLocal="xiaoyan";
	public String mEngineType = SpeechConstant.TYPE_CLOUD;

	private Toast mToast;
	public SharedPreferences mSharedPreferences;

	// 设置门限值 ： 门限值越低越容易被唤醒
	private int curThresh = 1450;
	private String keep_alive = "1";
	private String ivwNetMode = "0";
	//------------------------------
	public double lat=0.0;
	public double lng=0.0;
	public double pt2lat=0.0;
	public double pt2lng=0.0;

	@Override
	public void onCreate() {
		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		// 设置你申请的应用appid
		
		// 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
		
		StringBuffer param = new StringBuffer();
		param.append("appid="+getString(R.string.app_id));
		param.append(",");
		// 设置使用v5+
		param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
		SpeechUtility.createUtility(SpeechApp.this, param.toString());
		super.onCreate();
		/***
		 * 初始化定位sdk，建议在Application中创建
		 */
		locationService = new LocationService(getApplicationContext());
		mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
		SDKInitializer.initialize(getApplicationContext());

		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        // 初始化合成对象
		mIvw = VoiceWakeuper.createWakeuper(this, null);
        setParam();
        //getSocket();
	}

	/**
	 * 初始化监听。
	 */
	public InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				Log.d(TAG, "onInit: 初始化失败,错误码："+code);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}
		}
	};

	/**
	 * 合成回调监听。
	 */
	public SynthesizerListener mTtsListener = new SynthesizerListener() {

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
				Log.d(TAG, "onCompleted: 播放完成");
			} else if (error != null) {
				Log.d(TAG, "onCompleted: " + error.getPlainDescription(true));
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

	/**
	 * 参数设置
	 * @return
	 */
	public void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		//mTts.setParameter(SpeechConstant.PARAMS, null);
			//设置合成
			if(mEngineType.equals(SpeechConstant.TYPE_CLOUD))
			{

			//设置使用云端引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			//设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicerCloud);
		}else {
			//设置使用本地引擎
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			//设置发音人资源路径
			mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath());
			//设置发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicerLocal);
		}
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));

		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");

		// 清空参数
		mIvw.setParameter(SpeechConstant.PARAMS, null);
		// 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
		mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ curThresh);
		// 设置唤醒模式
		mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
		// 设置持续进行唤醒
		mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
		// 设置闭环优化网络模式
		mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
		// 设置唤醒资源路径
		mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
		// 设置唤醒录音保存路径，保存最近一分钟的音频
		mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
		mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
		// 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
		//mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

		//--------------------------------------------------------------
		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		mSharedPreferences = getSharedPreferences("com.iflytek.setting",
				Activity.MODE_PRIVATE);

		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		this.mTranslateEnable = mSharedPreferences.getBoolean( this.getString(R.string.pref_key_translate), false );
		if( mTranslateEnable ){
			Log.i( TAG, "translate enable" );
			mIat.setParameter( SpeechConstant.ASR_SCH, "1" );
			mIat.setParameter( SpeechConstant.ADD_CAP, "translate" );
			mIat.setParameter( SpeechConstant.TRS_SRC, "its" );
		}

		String lag = mSharedPreferences.getString("iat_language_preference",
				"mandarin");
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
			mIat.setParameter(SpeechConstant.ACCENT, null);

			if( mTranslateEnable ){
				mIat.setParameter( SpeechConstant.ORI_LANG, "en" );
				mIat.setParameter( SpeechConstant.TRANS_LANG, "cn" );
			}
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);

			if( mTranslateEnable ){
				mIat.setParameter( SpeechConstant.ORI_LANG, "cn" );
				mIat.setParameter( SpeechConstant.TRANS_LANG, "en" );
			}
		}
		//此处用于设置dialog中不显示错误码信息
		//mIat.setParameter("view_tips_plain","false");

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}

	//获取发音人资源路径
	public String getResourcePath(){
		StringBuffer tempBuffer = new StringBuffer();
		//合成通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
		tempBuffer.append(";");
		//发音人资源
		tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/"+ TtsDemoActivity.voicerLocal+".jet"));
		return tempBuffer.toString();
	}

	private String getResource() {
		final String resPath = ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+getString(R.string.app_id)+".jet");
		Log.d( TAG, "resPath: "+resPath );
		return resPath;
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
//				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	public void getSocket() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				piIP = GetPiIputil.getConnectedIP().get(0);
				try {
					socket = new Socket(piIP , 7653);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

    public static void addDestoryActivity(Activity activity, String activityName) {
        destroyMap.put(activityName, activity);
    }

    public static void destoryActivity(String activityName) {
        Set<String> keySet = destroyMap.keySet();
        if (keySet.size() > 0) {
            for (String key : keySet) {
                if (activityName.equals(key)) {
                    destroyMap.get(key).finish();
                }
            }
        }
    }
}
