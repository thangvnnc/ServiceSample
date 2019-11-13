package com.android.servicesample.stringee.notify;

import com.android.servicesample.stringee.log.LogStringee;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by luannguyen on 2/7/2018.
 */

public class PushNotifyServiceIdStringee extends FirebaseInstanceIdService {
    private final String TAG = "PushNtfServiceId";

    @Override
    public void onTokenRefresh() {
        LogStringee.error(TAG, "onTokenRefresh");
    }
}
