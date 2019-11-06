package com.android.servicesample.stringee.log;

import android.util.Log;

public class LogStringee {
    public static void error(String tag, String message) {
        Log.e(tag, message);
    }
}
