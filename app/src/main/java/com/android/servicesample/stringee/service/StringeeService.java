package com.android.servicesample.stringee.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.android.servicesample.App;
import com.android.servicesample.MainActivity;
import com.android.servicesample.R;
import com.android.servicesample.stringee.activity.IncomingCallActivity;
import com.android.servicesample.stringee.common.StringeeToken;
import com.android.servicesample.stringee.define.StringeeKeys;
import com.android.servicesample.stringee.define.StringeeSound;
import com.android.servicesample.stringee.define.TransferKeys;
import com.android.servicesample.stringee.log.LogStringee;
import com.android.servicesample.stringee.receive.TransferServiceReceiver;
import com.google.firebase.iid.FirebaseInstanceId;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StringeeService extends Service implements StringeeConnectionListener {

    // Static
    private static StringeeService instance = null;
    public static StringeeService getInstance() {
        if (instance == null) {
            instance = new StringeeService();
        }
        return instance;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, StringeeService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogStringee.error(TAG, "startForegroundService");
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }
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

    private TransferServiceReceiver transferServiceReceiver = new TransferServiceReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogStringee.error(TAG, "received");
            String key = intent.getStringExtra(TransferKeys.KEY);
            if (StringeeSound.SOUND_KEY.equals(key)) {
                String soundKey = intent.getStringExtra(StringeeSound.SOUND_KEY);
                playSound(soundKey);
            }
        }
    };

    private final static String TAG = "StringeeService";

    // Non static
    private Map<String, MediaPlayer> soundMap = new HashMap<>();
    public StringeeClient stringeeClient = null;
    public Map<String, StringeeCall> callsMap = new HashMap<>();
    private NotificationCompat.Builder notificationBuilder = null;
    private int notifyId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initRingStone();
        IntentFilter intentFilterTransfer = new IntentFilter("service.Broadcast");
        registerReceiver(transferServiceReceiver, intentFilterTransfer);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initStringee();
        initNotifyKeepServiceRunning(intent);
        reconnect();
        LogStringee.error(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        reconnect();
        LogStringee.error(TAG, "onTaskRemoved");
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
        unregisterReceiver(transferServiceReceiver);
        sendBroadcast(new Intent("RestartServiceReceiver"));
        LogStringee.error(TAG, "onDestroy");
    }

    @Override
    public void onConnectionConnected(StringeeClient stringeeClient, boolean b) {
        saveUserId(this, stringeeClient.getUserId());
        registerNotify();
        LogStringee.error(TAG, "onConnectionConnected");
//        updateNotification("Connected");
        LogStringee.toastAnywhere("Connected");
    }

    @Override
    public void onConnectionDisconnected(StringeeClient stringeeClient, boolean b) {
        LogStringee.error(TAG, "onConnectionDisconnected");
//        updateNotification("Disconnected");
//        unregisterNotify();
    }

    @Override
    public void onIncomingCall(StringeeCall stringeeCall) {
        LogStringee.error(TAG, "onIncomingCall");
        callsMap.put(stringeeCall.getCallId(), stringeeCall);
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.putExtra("call_id", stringeeCall.getCallId());
        startActivity(intent);
    }

    @Override
    public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
        LogStringee.error(TAG, "stringeeError : " + stringeeError.getMessage());
    }

    @Override
    public void onRequestNewToken(StringeeClient stringeeClient) {
        LogStringee.error(TAG, "onRequestNewToken");
        reconnect();
    }

    @Override
    public void onCustomMessage(String s, JSONObject jsonObject) {
        LogStringee.error(TAG, "onCustomMessage");
    }

    private void initRingStone() {
        MediaPlayer outgoingRing = MediaPlayer.create(this, R.raw.sound_outgoing);
        outgoingRing.setLooping(true);
        soundMap.put(StringeeSound.OUTGOING_RING, outgoingRing);

        MediaPlayer ring = MediaPlayer.create(this, R.raw.sound_ring);
        ring.setLooping(true);
        soundMap.put(StringeeSound.RING, ring);

        MediaPlayer incommingRing = MediaPlayer.create(this, R.raw.sound_incomming);
        incommingRing.setLooping(true);
        soundMap.put(StringeeSound.INCOMMING_RING, incommingRing);

        MediaPlayer busy = MediaPlayer.create(this, R.raw.sound_busy);
        busy.setLooping(false);
        soundMap.put(StringeeSound.BUSY, busy);

        MediaPlayer end = MediaPlayer.create(this, R.raw.sound_hangup);
        end.setLooping(false);
        soundMap.put(StringeeSound.END, end);
    }

    private void playSound(String key) {
        stopSound();
        if (!StringeeSound.OFF.equals(key)) {
            AudioManager mAudioManager = (AudioManager) App.getInstance().getSystemService(Context.AUDIO_SERVICE);
            if (StringeeSound.INCOMMING_RING.equals(key)) {
                mAudioManager.setMode(AudioManager.MODE_CURRENT);
                mAudioManager.setSpeakerphoneOn(true);
            }
            else {
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                mAudioManager.setSpeakerphoneOn(false);
            }
            soundMap.get(key).start();
        }
        else {
            LogStringee.error(TAG, "OFF sound");
        }
    }

    private void stopSound() {
        if(soundMap.get(StringeeSound.OUTGOING_RING).isPlaying()) {
            soundMap.get(StringeeSound.OUTGOING_RING).stop();
            soundMap.get(StringeeSound.OUTGOING_RING).prepareAsync();
        }
        if(soundMap.get(StringeeSound.RING).isPlaying()) {
            soundMap.get(StringeeSound.RING).stop();
            soundMap.get(StringeeSound.RING).prepareAsync();
        }
        if(soundMap.get(StringeeSound.INCOMMING_RING).isPlaying()) {
            soundMap.get(StringeeSound.INCOMMING_RING).stop();
            soundMap.get(StringeeSound.INCOMMING_RING).prepareAsync();
        }
        if(soundMap.get(StringeeSound.BUSY).isPlaying()) {
            soundMap.get(StringeeSound.BUSY).stop();
            soundMap.get(StringeeSound.BUSY).prepareAsync();
        }
        if(soundMap.get(StringeeSound.END).isPlaying()) {
            soundMap.get(StringeeSound.END).stop();
            soundMap.get(StringeeSound.END).prepareAsync();
        }
        AudioManager mAudioManager = (AudioManager) App.getInstance().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        mAudioManager.setSpeakerphoneOn(false);
    }

    private void initNotifyKeepServiceRunning(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogStringee.error(TAG, "initNotifyKeepServiceRunning");
            String chanelId = "chanelKeepService";
            notificationBuilder = new NotificationCompat.Builder(this, chanelId);
            Notification notification = notificationBuilder.build();
            startForeground(notifyId, notification);
        }
//        String input = intent.getStringExtra("inputExtra");
//        String chanelId = "chanelKeepService";
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notificationBuilder = new NotificationCompat.Builder(this, chanelId);
//                .setContentTitle("ServiceStringee")
//                .setContentText(input)
//                .setSmallIcon(R.drawable.ic_android_black_24dp)
//                .setContentIntent(pendingIntent);
//        Notification notification = notificationBuilder.build();
//        startForeground(notifyId, notification);
    }

    private void initNotifyService(Intent intent) {
        String input = intent.getStringExtra("inputExtra");
        String chanelId = "chanelIdNotify";
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationBuilder = new NotificationCompat.Builder(this, chanelId)
                .setContentTitle("ServiceStringee")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        startForeground(notifyId, notification);
    }

    private void updateNotification(String str) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder.setContentText(str);
        mNotificationManager.notify(notifyId, notificationBuilder.build());
    }

    private void initStringee() {
        stringeeClient = new StringeeClient(this);
        stringeeClient.setConnectionListener(this);
    }

    private void registerNotify() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        StringeeClient client = StringeeService.getInstance().stringeeClient;
        // Register the token to Stringee Server
        if (client != null && client.isConnected()) {
            client.registerPushToken(refreshedToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    LogStringee.error(TAG, "registerPushToken success");
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    LogStringee.error(TAG, "registerPushToken error: " + stringeeError.getMessage());
                }
            });
        }
    }

    private void unregisterNotify() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        StringeeClient client = StringeeService.getInstance().stringeeClient;
        // Register the token to Stringee Server
        if (client != null && client.isConnected()) {
            client.unregisterPushToken(refreshedToken, new StatusListener() {
                @Override
                public void onSuccess() {
                    LogStringee.error(TAG, "unregisterPushToken success");
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    LogStringee.error(TAG, "unregisterPushToken error: " + stringeeError.getMessage());
                }
            });
        }
    }

    public void reconnect() {
        LogStringee.error(TAG, "Reconnect");
        String userTokenId = loadUserId(App.getInstance());
        if (userTokenId != null && "".equals(userTokenId) == false) {
            String token = StringeeToken.create(userTokenId);
            if (!stringeeClient.isConnected()) {
                stringeeClient.connect(token);
            }
            else {
                LogStringee.error(TAG, "Already connected ");
            }
        }
    }


    public void connect(String userId) {
        saveUserId(App.getInstance(), userId);
        reconnect();
    }

    public void disconnect() {
        if (stringeeClient != null) {
            StringeeService.getInstance().rẹmoveUserId(this);
            stringeeClient.disconnect();
        }
        else {
            LogStringee.error(TAG, "Disconnect is null strineeClient ");
        }
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