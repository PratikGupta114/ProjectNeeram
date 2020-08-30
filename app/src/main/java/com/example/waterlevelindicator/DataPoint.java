package com.example.waterlevelindicator;

import android.provider.ContactsContract;

public class DataPoint {
    private float   distance;
    private float   voltage;
    private long    timeStamp;
    private float   change;
    private boolean motorStatus;

    public DataPoint() {
        this.distance = 0.0f;
        this.voltage = 0.0f;
        this.change = 0.0f;
        this.motorStatus = false;
        this.timeStamp = System.currentTimeMillis();
    }

    public DataPoint(float distance, float voltage) {
        this.distance = distance;
        this.voltage = voltage;
        this.change = 0.0f;
        this.motorStatus = false;
        this.timeStamp = System.currentTimeMillis();
    }

    public float getDistance() {
        return distance;
    }
    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getVoltage() {
        return voltage;
    }
    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getChange() {
        return change;
    }
    public void setChange(float change) {
        this.change = change;
    }

    public boolean isMotorStatus() {
        return motorStatus;
    }
    public void setMotorStatus(boolean motorStatus) {
        this.motorStatus = motorStatus;
    }
}