package com.iflytek.mscv5plusdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import static android.content.ContentValues.TAG;

public class HooliganActivity extends Activity {

    public static HooliganActivity instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
    }

    /**
     * 开启保活页面
     */
    public static void startHooligan(Context context) {
        Intent intent = new Intent(context, HooliganActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 关闭保活页面
     */
    public static void killHooligan() {
        if(instance != null) {
            instance.finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "HooliganActivityonRestart:.......................");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "HooliganActivityonPause:.........................");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "HooliganActivityonDestroy:.......................");
    }

}
