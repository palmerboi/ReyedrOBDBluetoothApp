package com.example.reube.reyedrobdbluetoothapp;

import android.util.Log;

import java.nio.charset.Charset;

public class CANbusProtocol implements OBDII {
    private BluetoothConnectionService bluetoothConnectionService;

    public CANbusProtocol (BluetoothConnectionService bluetoothConnectionService) {
        Log.d(TAG, "Using CAN protocol method set..");
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    @Override
    public void reset() {
        String message = "ATZ\r";
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        Log.d(TAG, "Resetting ELM327...");
        bluetoothConnectionService.write(bytes);
    }

    @Override
    public void setup() {
        try {
            String message = "ATS0\r";
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            Log.d(TAG, "Setting no spaces..");
            bluetoothConnectionService.write(bytes);
            Thread.sleep(200);
            message = "ATL0\r";
            bytes = message.getBytes(Charset.defaultCharset());
            Log.d(TAG, "Setting linefeed off..");
            bluetoothConnectionService.write(bytes);
            Thread.sleep(200);
            message = "ATSP0\r";
            bytes = message.getBytes(Charset.defaultCharset());
            Log.d(TAG, "Setting protocol automatically..");
            bluetoothConnectionService.write(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getSpeed() {
        String message = "010D\r";
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        Log.d(TAG, "retrieving speed data...");
        bluetoothConnectionService.write(bytes);
    }

    @Override
    public void getRPM() {
        String message = "010C\r";
        byte[] bytes = message.getBytes(Charset.defaultCharset());
        Log.d(TAG, "retrieving rpm data...");
        bluetoothConnectionService.write(bytes);
    }

    @Override
    public String processSpeedResponse(String data) {
        Log.d(TAG, "processing speed...");
        String processedResponse = data.substring(data.length()-3,data.length()-1);
        Log.d(TAG, "substring: " + processedResponse);
        if (processedResponse.length() == 2) {
            Integer byteA;
            try {
                byteA = Integer.parseInt(processedResponse, 16);
            } catch (NumberFormatException e) {
                return null;
            }
            Log.d(TAG, "decimal value of speed: " + byteA.toString());
            processedResponse = byteA.toString() + " km/h";
            Log.d(TAG, processedResponse);
            return processedResponse;
        }
        return null;
    }

    @Override
    public String processRPMResponse(String data) {
        Log.d(TAG, "processing rpm");
        String processedResponse = data.substring(data.length()-5, data.length()-1);
        Log.d(TAG, processedResponse);
        if (processedResponse.length() == 4) {
            String pRA = processedResponse.substring(0,2);
            String pRB = processedResponse.substring(2);
            Integer byteA, byteB;
            try {
                byteA = Integer.parseInt(pRA, 16);
                byteB = Integer.parseInt(pRB, 16);
            } catch (NumberFormatException e) {
                return null;
            }
            Integer RPM = (256*byteA + byteB)/4;
            Log.d(TAG, "decimal value of rpm: " + RPM.toString());
            processedResponse = RPM.toString() + " revs/min";
            Log.d(TAG, processedResponse);
            return processedResponse;
        }
        return null;
    }
}
