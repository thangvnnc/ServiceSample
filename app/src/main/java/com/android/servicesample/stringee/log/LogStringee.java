package com.android.servicesample.stringee.log;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.servicesample.App;

public class LogStringee {
    public static void error(String tag, String message) {
//        toastAnywhere(tag + " | " + message);
        Log.e(tag, message);
    }

    public static void toastAnywhere(final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(App.getInstance().getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }
}
