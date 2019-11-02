package com.android.servicesample;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.android.servicesample.stringee.service.TransferServiceReceiver;

public class ServiceSample extends Service {
    private static final String TAG = "ServiceSample";

    private static ServiceSample intance = null;
    public static ServiceSample getInstance() {
        return intance;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ServiceSample.class);
        context.startService(intent);
    }

    public static void stop() {
        ServiceSample serviceSample = getInstance();
        if (serviceSample != null) {
            serviceSample.stopSelf();
        }
    }

    public static boolean isNull() {
        return intance == null;
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ServiceSample.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private TransferServiceReceiver serviceReceiver = new TransferServiceReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "received");
            ServiceSample serviceSample = ServiceSample.getInstance();
            if (serviceSample.isNull()) {
                Log.e(TAG, "ServiceSample is null");
                return;
            }

            // Call process service
            serviceSample.process();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intance = this;
        IntentFilter intentFilter = new IntentFilter("service.Broadcast");
        registerReceiver(serviceReceiver, intentFilter);
        Log.e(TAG, "Service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        intance = null;
        unregisterReceiver(serviceReceiver);
        Log.e(TAG, "onDestroy");
    }

    public void process() {
        Log.e(TAG, "processing...");
        Intent intent = new Intent();
        intent.setAction("main.Broadcast");
        sendBroadcast(intent);
    }
}


