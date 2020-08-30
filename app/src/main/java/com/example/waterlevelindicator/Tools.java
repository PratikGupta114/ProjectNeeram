package com.example.waterlevelindicator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Tools {
    public static String getTodaysDate() {
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return modifiedDate;
    }

    public static String getCurrentHour() {
        return Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null) && (networkInfo.isConnected());
    }

}
