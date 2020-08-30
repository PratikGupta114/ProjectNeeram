package com.example.waterlevelindicator;

import android.app.Activity;

import com.github.mikephil.charting.charts.LineChart;

public class LineChartManager {

    public static final int ACTIVITY_MAIN = 324234;

    private MainActivity    activity;
    private LineChart   lineChart;

    public LineChartManager(Activity activity, int ActivityCode) {
        if(ActivityCode == ACTIVITY_MAIN)
            this.activity = (MainActivity ) activity;
    }

    // TODO - create a method to accept new data and display it on the graph.

}
