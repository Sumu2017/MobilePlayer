package com.yml.mobileplayer;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.xutils.x;

public class MyApplication extends Application {

    private static Gson sGson;

    public static Gson getGson() {
        if (sGson==null){
            sGson=new Gson();
        }
        return sGson;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5b1534cf");
        Fresco.initialize(this);
    }
}
