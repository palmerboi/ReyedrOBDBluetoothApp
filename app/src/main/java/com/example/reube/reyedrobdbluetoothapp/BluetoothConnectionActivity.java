package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BluetoothConnectionActivity extends Activity {
    private static final String TAG = "BluetoothConnectionAct";
    private static final int REQUEST_ENABLE_BT = 1 ;
    private String connectorType;
    private BluetoothAdapter bluetoothAdapter;
    private Map<String, BluetoothDevice> discoveredDevices = new HashMap<String, BluetoothDevice>();
    private ArrayList<String> discoveredDeviceNames = new ArrayList();
    private Map<String, String> pairedDevices = new HashMap<>();
    private ArrayList<String> pairedDeviceNames = new ArrayList<>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "Device found..");
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device: " + device.getName() + "/" + device.getAddress());
                discoveredDevices.put(device.getName(), device);
                discoveredDeviceNames.add(device.getName());
                setDiscoveredSpinner();
            }
        }
    };

    public void setDiscoveredSpinner() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, discoveredDeviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setPairedSpinner() {
        Spinner spinner = (Spinner)findViewById(R.id.spinner3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pairedDeviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra("ConnectorSelection");
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

    public void getPairedDevices(View view) {
        Set<BluetoothDevice> pairedDeviceSet = bluetoothAdapter.getBondedDevices();
        Log.d(TAG, "Searching currently paired devices..");
        Spinner spinner = (Spinner)findViewById(R.id.spinner3);
        if (pairedDeviceNames.size() == 0) {
            pairedDeviceNames.add("Select device from list:");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pairedDeviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDeviceSet) {
                Log.d(TAG, "PairedDevice: " +device.getName() + "/" + device.getAddress() );
                pairedDevices.put(device.getName(), device.getAddress());
                pairedDeviceNames.add(device.getName());
                setPairedSpinner();
            }
        }
    }

    public void discoverDevices(View view) {
        bluetoothAdapter.cancelDiscovery();
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(receiver, filter);
        bluetoothAdapter.startDiscovery();
        Log.d(TAG, "Discovering...");
        Spinner spinner = (Spinner)findViewById(R.id.spinner2);
        if (discoveredDeviceNames.size() == 0) {
            discoveredDeviceNames.add("Select device from list:");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, discoveredDeviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void connectToDevice(View view) {
        Spinner spinner = (Spinner)findViewById(R.id.spinner2);
        String deviceName = spinner.getSelectedItem().toString();
        BluetoothDevice device = discoveredDevices.get(deviceName);
        Log.d(TAG, "connectToDevice: " + device.getName() + " " + device.getAddress());
        Intent intent = new Intent(this, OBDDashboardActivity.class);
        intent.putExtra("btDevice", device);
        intent.putExtra("ConnectorSelection", connectorType);
        startActivity(intent);
        }
    }

