package com.android.servicesample.stringee.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.android.servicesample.stringee.log.LogStringee;
import com.android.servicesample.stringee.service.StringeeService;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogStringee.error("RestartServiceReceiver", "restart");
        Intent intentService = new Intent(context, StringeeService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService);
        } else {
            context.startService(intentService);
        }
    }
}
