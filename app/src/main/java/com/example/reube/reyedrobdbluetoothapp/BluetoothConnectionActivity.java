package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BluetoothConnectionActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1 ;
    private String connectorType;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> discoveredDevices = new ArrayList();

    // Create a BroadcastReceiver for ACTION_FOUND.
//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            Log.d("Stuff", "onReceive: ");;
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Log.d("Device_Found", "Device " + device.getName() + " found...");
//                discoveredDevices.add(device);
//            }
//        }
//    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("stuff", "onReceive: ACTION FOUND.");
            Log.d("sheet", "action is: " + action.toString());
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device);
                Log.d("other stuff", "onReceive: " + device.getName() + ": " + device.getAddress());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(ConnectorSelectionActivity.EXTRA_SELECTION);
        connectorType = message;
        Toast.makeText(this, message + " was selected" , Toast.LENGTH_LONG).show();
        // Get Bluetooth adapter on device
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button button = findViewById(R.id.button2);
        if (!bluetoothAdapter.isEnabled()) {
            button.setText(getResources().getString(R.string.enable_bluetooth));
        }
        if (bluetoothAdapter.isEnabled()) {
            button.setText(getResources().getString(R.string.disable_bluetooth));
        }
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    public void enableDisableBluetooth(View view) {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth not supported on this device!" , Toast.LENGTH_LONG).show();
        }
        Button button = findViewById(R.id.button2);
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            button.setText(getResources().getString(R.string.disable_bluetooth));
        }
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            button.setText(getResources().getString(R.string.enable_bluetooth));
        }
    }

    public void discoverDevices(View view) {
        bluetoothAdapter.cancelDiscovery();
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
        Log.d("shit happening", "discoverDevices: ");
    }
}
