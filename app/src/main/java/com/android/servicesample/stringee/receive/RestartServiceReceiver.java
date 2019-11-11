package com.android.servicesample.stringee.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.servicesample.stringee.log.LogStringee;

public class RestartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogStringee.error("RestartServiceReceiver", "auto restart");
    }
}
