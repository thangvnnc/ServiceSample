package com.android.servicesample;

import android.app.Application;

import com.android.servicesample.stringee.service.StringeeService;

public class App extends Application {
    private static Application instance;

    public static Application getInstance() {
        return instance;
    }

    @Override public void onCreate() {
        super.onCreate();
        instance = this;

        if (!StringeeService.isRunning(this)) {
            StringeeService.start(this);
        }
    }
}
