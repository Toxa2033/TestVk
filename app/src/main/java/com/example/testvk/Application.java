package com.example.testvk;

import com.vk.sdk.VKSdk;

/**
 * Created by 95tox on 25.02.2016.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
