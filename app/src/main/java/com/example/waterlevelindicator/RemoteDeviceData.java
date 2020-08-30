package com.example.waterlevelindicator;

public class RemoteDeviceData {
    private int uptimeHour;
    private int uptimeMinutes;
    private int uptimeSeconds;
    private boolean motorStatus;
    private int waterLevelPercentage;
    private long timeStamp;

    public int getUptimeHour() {
        return uptimeHour;
    }
    public void setUptimeHour(int uptimeHour) {
        this.uptimeHour = uptimeHour;
    }

    public int getUptimeMinutes() {
        return uptimeMinutes;
    }
    public void setUptimeMinutes(int uptimeMinutes) {
        this.uptimeMinutes = uptimeMinutes;
    }

    public int getUptimeSeconds() {
        return uptimeSeconds;
    }
    public void setUptimeSeconds(int uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public boolean isMotorStatus() {
        return motorStatus;
    }
    public void setMotorStatus(boolean motorStatus) {
        this.motorStatus = motorStatus;
    }

    public int getWaterLevelPercentage() {
        return waterLevelPercentage;
    }
    public void setWaterLevelPercentage(int waterLevelPercentage) {
        this.waterLevelPercentage = waterLevelPercentage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
