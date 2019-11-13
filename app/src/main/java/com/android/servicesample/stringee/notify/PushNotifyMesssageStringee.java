package com.android.servicesample.stringee.notify;

import android.content.Intent;

import com.android.servicesample.MainActivity;
import com.android.servicesample.stringee.log.LogStringee;
import com.android.servicesample.stringee.service.StringeeService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.stringee.StringeeClient;

/**
 * Created by luannguyen on 2/7/2018.
 */

public class PushNotifyMesssageStringee extends FirebaseMessagingService {

    private final String TAG = "PushNotifyMsgStringee";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Connect Stringee Server here then the client receives an incoming call.
        // In this sample, we only start MainActivity and connect Stringee Server in MainActivity.
        StringeeClient client = StringeeService.getInstance().stringeeClient;
        if (remoteMessage.getData().size() > 0) {
            LogStringee.error(TAG, "Message data payload: " + remoteMessage.getData());
            String pushFromStringee = remoteMessage.getData().get("stringeePushNotification");
            if (pushFromStringee != null) {
                if (client == null) { // Check whether the app is not alive
                    startActivity(new Intent(this, MainActivity.class));
                }
            }
        }
    }
}
