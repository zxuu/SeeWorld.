package com.iflytek.mscv5plusdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

public class TtsDemoActivity extends Activity implements OnClickListener {
	private static String TAG = TtsDemoActivity.class.getSimpleName();
	// 语音合成对象
	private SpeechSynthesizer mTts;

	// 默认云端发音人
	public static String voicerCloud="xiaoyan";
	// 默认本地发音人
	public static String voicerLocal="xiaoyan";
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	
	private Toast mToast;
	private SharedPreferences mSharedPreferences;

	String texts = "";

	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ttsdemo);
		initLayout();

		// 初始化合成对象
//		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
//		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
//		mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
	}

	/**
	 * 初始化Layout。
	 */
	private void initLayout() {
		findViewById(R.id.tts_play).setOnClickListener(this);
		texts = getResources().getString(R.string.text_tts_source);
		mTts = ((SpeechApp) getApplication()).mTts;

	}	

	@Override
	public void onClick(View view) {
		if( null == mTts ){
			// 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
			this.showTip( "创建对象失败，请确认 libmsc.so 放置正确，\n 且有调用 createUtility 进行初始化" );
			return;
		}
		
		switch(view.getId()) {
		// 开始合成
		// 收到onCompleted 回调时，合成结束、生成合成音频
        // 合成的音频格式：只支持pcm格式
		case R.id.tts_play:
			String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
			int code = mTts.startSpeaking(text, mTtsListener);
//			/** 
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
			
			if (code != ErrorCode.SUCCESS) {
				showTip("语音合成失败,错误码: " + code);	
			}
			break;
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

	private void showTip(final String str){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if( null != mTts ){
			mTts.stopSpeaking();
			// 退出时释放连接
			mTts.destroy();
		}
	}

}
