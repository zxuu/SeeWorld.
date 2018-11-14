package com.iflytek.mscv5plusdemo;

import android.os.Bundle;
import android.widget.Toast;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.NaviLatLng;


public class WalkRouteCalculateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_navi);
        ((SpeechApp) getApplication()).addDestoryActivity(this,"WalkRouteCalculateActivity");
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        mAMapNaviView.setNaviMode(AMapNaviView.NORTH_UP_MODE);

    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        double lat; double lng; double pt2lat; double pt2lng;
        lat = ((SpeechApp) getApplication()).lat;
        lng = ((SpeechApp) getApplication()).lng;
        pt2lat = ((SpeechApp) getApplication()).pt2lat;
        pt2lng = ((SpeechApp) getApplication()).pt2lng;
        Toast.makeText(this, "" + lat, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "" + lng, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "" + pt2lat, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "" + pt2lng, Toast.LENGTH_SHORT).show();
        mAMapNavi.calculateWalkRoute(new NaviLatLng(((SpeechApp) getApplication()).lat, ((SpeechApp) getApplication()).lng), new NaviLatLng(((SpeechApp) getApplication()).pt2lat, ((SpeechApp) getApplication()).pt2lng));

    }

    @Override
    public void onCalculateRouteSuccess(int[] ids) {
        super.onCalculateRouteSuccess(ids);
        mAMapNavi.startNavi(NaviType.GPS);
    }
}
