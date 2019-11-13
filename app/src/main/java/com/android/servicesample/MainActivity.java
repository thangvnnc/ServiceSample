package com.android.servicesample;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.servicesample.stringee.activity.OutgoingCallActivity;
import com.android.servicesample.stringee.log.LogStringee;
import com.android.servicesample.stringee.service.StringeeService;
import com.android.servicesample.stringee.receive.TransferServiceReceiver;
import com.stringee.StringeeClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private TransferServiceReceiver mainReceiver = new TransferServiceReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogStringee.error(TAG, "received");
            Toast.makeText(getBaseContext(), "A", Toast.LENGTH_SHORT).show();
        }
    };

    private EditText edtTo = null;
    private EditText edtUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        if ("xiaomi".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
//            Intent autostartIntent = new Intent();
//            autostartIntent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//            startActivity(autostartIntent);
//        }

        IntentFilter intentFilter = new IntentFilter("main.Broadcast");
        registerReceiver(mainReceiver, intentFilter);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        edtTo = findViewById(R.id.edtTo);
        edtUserId = findViewById(R.id.edtUserId);
        StringeeClient client = StringeeService.getInstance().stringeeClient;
        if (client != null) {
            edtUserId.setText(client.getUserId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        StringeeClient client = StringeeService.getInstance().stringeeClient;
        if (client != null) {
            edtUserId.setText(client.getUserId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mainReceiver);
    }

    @Override
    public void onClick(View view) {
        StringeeClient client = StringeeService.getInstance().stringeeClient;
        switch (view.getId()) {
            case R.id.btn1:
                if (StringeeService.isRunning(this)) {
                    StringeeService.getInstance().connect(edtUserId.getText().toString());
                }

                break;

            case R.id.btn2:
                StringeeService.getInstance().disconnect();
                break;

            case R.id.btn3:
                if (client.isConnected()) {
                    Intent intent = new Intent(MainActivity.this, OutgoingCallActivity.class);
                    intent.putExtra("from", client.getUserId());
                    intent.putExtra("to", edtTo.getText().toString());
                    intent.putExtra("is_video_call", false);
                    startActivity(intent);
                }
                break;

            case R.id.btn4:
                if (client.isConnected()) {
                    Intent intent = new Intent(MainActivity.this, OutgoingCallActivity.class);
                    intent.putExtra("from", client.getUserId());
                    intent.putExtra("to", edtTo.getText().toString());
                    intent.putExtra("is_video_call", true);
                    startActivity(intent);
                }
                break;
        }
    }
}
