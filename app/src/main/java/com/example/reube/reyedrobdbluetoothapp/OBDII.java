package com.example.reube.reyedrobdbluetoothapp;

public interface OBDII {
    static final String TAG = "OBDII";
    void reset();
    void setup();
    void getSpeed();
    void getRPM();
    String processSpeedResponse(String data);
    String processRPMResponse(String data);
}
