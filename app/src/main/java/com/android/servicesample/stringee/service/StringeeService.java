package com.android.servicesample.stringee.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.servicesample.stringee.activity.IncomingCallActivity;
import com.android.servicesample.stringee.common.StringeeToken;
import com.android.servicesample.stringee.define.StringeeKeys;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StringeeService extends Service implements StringeeConnectionListener {

    // Static
    private static StringeeService instance = null;
    public static StringeeService getInstance() {
        return instance;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, StringeeService.class);
        context.startService(intent);
    }

    public static void stop() {
        StringeeService stringeeService = getInstance();
        if (stringeeService != null) {
            stringeeService.stopSelf();
        }
    }

    public static boolean isNull() {
        return instance == null;
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StringeeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private TransferServiceReceiver serviceReceiver = new TransferServiceReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "received");
            StringeeService stringeeService = StringeeService.getInstance();
            if (stringeeService.isNull()) {
                Log.e(TAG, "ServiceSample is null");
                return;
            }

            // Call process service
            stringeeService.process();
        }
    };

    // Non static
    private final static String TAG = "StringeeService";
    private MediaPlayer mediaPlayer = null;
    private String userTokenId = null;
    public StringeeClient stringeeClient = null;
    public Map<String, StringeeCall> callsMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        IntentFilter intentFilter = new IntentFilter("service.Broadcast");
        registerReceiver(serviceReceiver, intentFilter);
    }

    public void process() {
        startRing();
        Log.e(TAG, "processing...");
//        Intent intent = new Intent();
//        intent.setAction("main.Broadcast");
//        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initRingStone();
        initStringee();
        Log.e(TAG, "onStartCommand");
        String userId = loadUserId(this);
        if (userId != null && "".equals(userId) == false) {
            userTokenId = userId;
            refreshToken();
        }
        if (userTokenId != null) {
            refreshToken();
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (instance != null) {
            instance = null;
        }

        if (stringeeClient != null) {
            stringeeClient.disconnect();
            stringeeClient = null;
        }
        unregisterReceiver(serviceReceiver);
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
        saveUserId(this, stringeeClient.getUserId());
        Log.e(TAG, "onConnectionConnected");
    }

    @Override
    public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
        Log.e(TAG, "onConnectionDisconnected");
        rẹmoveUserId(this);
    }

    @Override
    public void onIncomingCall(StringeeCall stringeeCall) {
        Log.e(TAG, "onIncomingCall");
        callsMap.put(stringeeCall.getCallId(), stringeeCall);
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("call_id", stringeeCall.getCallId());
        startActivity(intent);
    }

    @Override
    public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
        Log.e(TAG, "stringeeError : " + stringeeError.getMessage());
    }

    @Override
    public void onRequestNewToken(StringeeClient stringeeClient) {
        Log.e(TAG, "onRequestNewToken");
    }

    @Override
    public void onCustomMessage(String s, JSONObject jsonObject) {
        Log.e(TAG, "onCustomMessage");
    }

    private void initRingStone() {
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        mediaPlayer.setLooping(true);
    }

    private void startRing() {
        mediaPlayer.start();
    }

    private void stopRing() {
        mediaPlayer.stop();
    }

    private void initStringee() {
        stringeeClient = new StringeeClient(this);
        stringeeClient.setConnectionListener(this);
    }

    private void refreshToken() {
        String token = StringeeToken.create(userTokenId);
        stringeeClient.connect(token);
    }


    public void connect(String userId) {
        userTokenId = userId;
        refreshToken();
    }

    public void disconnect() {
        stringeeClient.disconnect();
    }


    public String loadUserId(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Stringee", Context.MODE_PRIVATE);
        return sharedPreferences.getString(StringeeKeys.STRINGEE_KEY_SAVE_USER_ID, "");
    }

    public void saveUserId(Context context, String userId){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Stringee", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(StringeeKeys.STRINGEE_KEY_SAVE_USER_ID, userId);
        editor.apply();
    }

    public void rẹmoveUserId(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Stringee", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(StringeeKeys.STRINGEE_KEY_SAVE_USER_ID);
        editor.apply();
    }
}
