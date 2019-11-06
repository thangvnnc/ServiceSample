package com.android.servicesample.stringee.notify;

import com.android.servicesample.stringee.log.LogStringee;
import com.android.servicesample.stringee.service.StringeeService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.stringee.StringeeClient;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

/**
 * Created by luannguyen on 2/7/2018.
 */

public class PushNotifyServiceIdStringee extends FirebaseInstanceIdService {
    private final String TAG = "PushNtfServiceId";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        StringeeClient client = StringeeService.getInstance().stringeeClient;
        // Register the token to Stringee Server
        if (client != null && client.isConnected()) {
            client.registerPushToken(refreshedToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    LogStringee.error(TAG, "onTokenRefresh success");
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    LogStringee.error(TAG, "onTokenRefresh error: " + stringeeError.getMessage());
                }
            });
        } else {
            // Handle your code here
        }
    }
}
