package com.iflytek.speech.util;

import java.util.Calendar;

public class GetTime {
    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        String time = "当前时间是" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "月"
                + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "号"
                + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + "时"
                + String.valueOf(calendar.get(Calendar.MINUTE)) + "分";
        return time;
    }
}
