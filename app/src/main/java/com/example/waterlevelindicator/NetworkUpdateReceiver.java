package com.example.waterlevelindicator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkUpdateReceiver extends BroadcastReceiver {
    private MainActivity activity;
    private NetworkStatusChangeListener networkStatusChangeListener;

    public NetworkUpdateReceiver(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOnline = Tools.isDeviceOnline(activity.getApplicationContext());
        if (this. networkStatusChangeListener != null) {
            try {
                this.networkStatusChangeListener.onNetworkStatusChange(isOnline);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setNetworkStatusChangeListener(NetworkStatusChangeListener networkStatusChangeListener){
        this.networkStatusChangeListener = networkStatusChangeListener;
    }
}
