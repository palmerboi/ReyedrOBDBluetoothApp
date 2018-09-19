package com.example.reube.reyedrobdbluetoothapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1 ;
    private String connectorType;
    private BluetoothAdapter bluetoothAdapter;
    private Map<String, BluetoothDevice> discoveredDevices = new HashMap<String, BluetoothDevice>();
    private ArrayList<String> discoveredDeviceNames = new ArrayList();
    private Map<String, String> pairedDevices = new HashMap<>();
    private ArrayList<String> pairedDeviceNames = new ArrayList<>();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("reciever", "Device found..");
            if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                Log.d("reciever", "Device: " + device.getName() + "/" + device.getAddress());
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
        Log.d("getPairedDevices", "Searching currently paired devices..");
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
                Log.d("getPairedDevices", "PairedDevice: " +device.getName() + "/" + device.getAddress() );
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
        Log.d("discoverDevices", "Discovering...");
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
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.run();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e("ConnectThread", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.d("run", "run: attempting to connect to device");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("run", "Could not close the client socket", closeException);
                }
                Log.d("run", "run: device successfully connected");
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("cancel", "Could not close the client socket", e);
            }
        }
    }
}
