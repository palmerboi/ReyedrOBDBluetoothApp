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
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.content.LocalBroadcastManager;
import java.nio.charset.Charset;

public class OBDDashboardActivity extends Activity {
    private static final String TAG = "OBDDashboardAct";
    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothDevice bluetoothDevice;
    private OBDII obdProtocol;
    String connectorType;
    private boolean gettingData;
    private int state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obddashboard);
        Intent intent = getIntent();
        connectorType = intent.getStringExtra("ConnectorSelection");
        bluetoothDevice = intent.getExtras().getParcelable("btDevice");
        bluetoothConnectionService = new BluetoothConnectionService(this);
        bluetoothConnectionService.startClient(bluetoothDevice);
        if (connectorType.equalsIgnoreCase("Harley Davidson/6-pin")) {
            obdProtocol = new CANbusProtocol(bluetoothConnectionService);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("incomingMessage"));
        gettingData = false;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            Log.d(TAG, "onReceive: " + text);
            if (text.contains("ATZ")) {
                if (gettingData == true) {
                    Log.d(TAG, "system rest, setting protocol..");
                    obdProtocol.setup();
                    state = 0;
                }
            } else if (text.contains("ATSP0")) {
                    Log.d(TAG, "setup complete, getting data now..");
                    obdProtocol.getSpeed();
            } else if (text.contains("SEARCHING")) {
                if (state == 0) {
                    state = 1;
                    obdProtocol.getRPM();
                } else if (state == 1) {
                    state = 0;
                    obdProtocol.getSpeed();
                } else if (state == 2) {
                    Log.d(TAG, "data collection stopped");
                }
            } else if (text.contains("0C")||text.contains("0D")) {
                Log.d(TAG, "data recieved");
                if (state == 0) {
                    state = 1;
                    TextView textView = findViewById(R.id.textView3);
                    textView.setText(obdProtocol.processSpeedResponse(text));
                    obdProtocol.getRPM();
                } else if (state == 1) {
                    state = 0;
                    TextView textView = findViewById(R.id.textView7);
                    textView.setText(obdProtocol.processRPMResponse(text));
                    obdProtocol.getSpeed();
                } else if (state == 2) {
                    Log.d(TAG, "data collection stopped");
                }
            }
        }
    };

    public void startGettingData(View view) {
        Button button = findViewById(R.id.button4);
        if (gettingData == false) {
            Log.d(TAG, "starting collection");
            gettingData = true;
            obdProtocol.reset();
            button.setText(getResources().getString(R.string.stop_collect_data));
        } else if (gettingData == true) {
            Log.d(TAG, "stopping collection");
            gettingData = false;
            state = 2;
            button.setText(getResources().getString(R.string.collect_data));
        }
    }
}

