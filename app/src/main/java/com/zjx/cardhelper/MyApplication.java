package com.zjx.cardhelper;

import android.app.Application;

import com.ryx.card_api.core.CardManager;

public class MyApplication  extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CardManager.openDebug();
        CardManager.init(this);
    }
}
