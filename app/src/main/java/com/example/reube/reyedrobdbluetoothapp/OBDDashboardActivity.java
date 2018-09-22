package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.support.v4.content.LocalBroadcastManager;
import java.nio.charset.Charset;

public class OBDDashboardActivity extends Activity {
    private static final String TAG = "OBDDashboardAct";
    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothDevice bluetoothDevice;
    private String connectorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obddashboard);
        Intent intent = getIntent();
        connectorType = intent.getStringExtra("ConnectorSelection");
        bluetoothDevice = intent.getExtras().getParcelable("btDevice");
        bluetoothConnectionService = new BluetoothConnectionService(this);
        bluetoothConnectionService.startClient(bluetoothDevice);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("incomingMessage"));
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.d(TAG, "onReceive: " + text);
            TextView textView = findViewById(R.id.textView4);
            textView.setText(text);
        }
    };

    public void send(View view) {
        String message = "ATZ\r";
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        Log.d(TAG, "send: "+ bytes);
        bluetoothConnectionService.write(bytes);
    }
}
