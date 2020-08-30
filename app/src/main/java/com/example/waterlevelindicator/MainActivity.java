package com.example.waterlevelindicator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.john.waveview.WaveView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements RemoteConnectivityInterface {

    public static final String TAG = "MainActivity";

    public static final int BANNER_CONNECTED = -132;
    public static final int BANNER_DISCONNECTED = -124;
    public static final int BANNER_INVISIBLE = -120;
    public static final int TURN_MOTOR_ON_REQUEST = 78;
    public static final int TURN_MOTOR_OFF_REQUEST = 79;
    public static final int ACK_SUCCESS = 1;
    public static final int ACK_FAILURE = 0;

    private Toolbar     toolbar;
    private TextView    percentLastChangeText, currentWaterLevelText, lastMotorTurnedTimeText, deviceUptimeText, progressText;
    private Button      motorButton;
    private WaveView    tankView;
    private LineChart   levelChart;
    private ProgressBar loadProgressBar;
    private CoordinatorLayout    layout;
    private LinearLayout         bannerView;
    private TextView             bannerText;

    private CustomDialog    customDialog;
    private NetworkUpdateReceiver updateReceiver;

    private FirebaseDatabase    firebaseDatabase;
    private DatabaseReference   databaseReference, rxTopic, txTopic, deviceInfoReference;

    private DataPoint   currentData;
    private ArrayList<DataPoint>    dataPoints;
    private boolean     isGarbageStatusData = true;
    private boolean     isGarbageAckData = true;
    private boolean     isMotorOn = false;
    private boolean     remoteDeviceConnectivityStatus = true;
    private volatile long recentTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recentTimestamp = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = MainActivity.this.getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(MainActivity.this , R.color.White));
        }

        setupUI();
        monitorRemoteDeviceConnectivity();

        firebaseDatabase = FirebaseDatabase.getInstance();
        deviceInfoReference = firebaseDatabase.getReference().child("water_level_status");
        rxTopic = firebaseDatabase.getReference().child("RX_TOPIC");
        txTopic = firebaseDatabase.getReference().child("TX_TOPIC");
        databaseReference = firebaseDatabase.getReference().child("water_level_data");
        customDialog = new CustomDialog(MainActivity.this);
        updateReceiver = new NetworkUpdateReceiver(MainActivity.this);

        fetchData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        deviceInfoReference.addValueEventListener(statusEventListener);
        txTopic.addValueEventListener(ackEventListener);
        registerReceiver(updateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        motorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog.showProgressDialog("Turning Motor "+( (isMotorOn) ? "Off" : "On" ));
                DecimalFormat df = new DecimalFormat("00000");
                String requestCode = df.format(gen()) + "-" + ((isMotorOn) ? TURN_MOTOR_OFF_REQUEST : TURN_MOTOR_ON_REQUEST);
                rxTopic.setValue( requestCode )
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if ( task.isSuccessful() ) {
                                    Log.e(TAG, "Could successfully Update request");
                                }
                            }
                        });
            }
        });

        updateReceiver.setNetworkStatusChangeListener(new NetworkStatusChangeListener() {
            @Override
            public void onNetworkStatusChange(boolean isDeviceOnline) {
                String snackString = (isDeviceOnline) ? "Connected to Internet" : "Lost Internet Connection";
                showSnackbar(snackString);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceInfoReference.removeEventListener(statusEventListener);
        txTopic.removeEventListener(ackEventListener);
        isGarbageAckData = true;
        isGarbageStatusData = true;
        unregisterReceiver(updateReceiver);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setMessage("Are you sure to exit ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onRemoteDeviceDisconnected() {
        updateBanner(BANNER_DISCONNECTED);
        Log.e(TAG, "Monitor Thread : Device disconnected");
    }

    @Override
    public void onRemoteDeviceConnected() {
        updateBanner(BANNER_CONNECTED);
        Log.e(TAG, "Sleep for 3 seconds");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Sleeping done.");
                Log.e(TAG, "Hiding banner.");
                updateBanner(BANNER_INVISIBLE);
            }
        }, 5000);
    }

    private ValueEventListener statusEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue() != null && !isGarbageStatusData) {
                recentTimestamp = System.currentTimeMillis();
                RemoteDeviceData remoteDeviceData = snapshot.getValue(RemoteDeviceData.class);
                updateUI(remoteDeviceData);
            }
            isGarbageStatusData = false;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private ValueEventListener ackEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.getValue() != null && !isGarbageAckData) {
                String ackString = (String) snapshot.getValue();
                int acknowledgement = Integer.parseInt(ackString.split("-")[1]);
                if(acknowledgement == ACK_SUCCESS) {}
                else if (acknowledgement == ACK_FAILURE) {
                    customDialog.hideDialogBox();
                    Toast.makeText(MainActivity.this, "Action Failed !", Toast.LENGTH_SHORT).show();
                }
            }
            isGarbageAckData = false;
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void fetchData() {

    }

    private void showSnackbar(String snackString) {
        Snackbar snackbar = Snackbar.make(layout, snackString, Snackbar.LENGTH_LONG);
        TextView snackTextView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        Typeface font = ResourcesCompat.getFont(getApplicationContext(), R.font.product_sans_regular);
        snackTextView.setTypeface(font);
        snackbar.show();
    }

    private void setupUI() {
        layout = findViewById(R.id.mainLayout);
        toolbar = findViewById(R.id.toolbar1);
        percentLastChangeText = findViewById(R.id.tv1_percentLastChange);
        currentWaterLevelText = findViewById(R.id.tv1_currentWaterLevel);
        lastMotorTurnedTimeText = findViewById(R.id.tv1_lastMotorTurnedTime);
        deviceUptimeText = findViewById(R.id.tv1_deviceUptimeText);
        motorButton = findViewById(R.id.bt1_motorButton);
        progressText = findViewById(R.id.loadingText);
        loadProgressBar = findViewById(R.id.loadProgress);
        levelChart = findViewById(R.id.lineChart);
        tankView = findViewById(R.id.tankView);
        bannerText = findViewById(R.id.bannerText);
        bannerView = findViewById(R.id.bannerView);

        levelChart.setVisibility(View.GONE);
        loadProgressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void monitorRemoteDeviceConnectivity() {
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    long diff = 0L;
                    synchronized (this) {
                        diff = System.currentTimeMillis() - recentTimestamp;
                    }
                    if(diff > 8000L) {
                        // executed when the last message received was 8s ago.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(remoteDeviceConnectivityStatus) {
                                    remoteDeviceConnectivityStatus = false;
                                    onRemoteDeviceDisconnected();
                                }
                            }
                        });
                    } else if(bannerView.getVisibility() == View.VISIBLE && !remoteDeviceConnectivityStatus) {
                        // executed when banner shows remote device is disconnected and device actually gets connected.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "Monitor Thread : Device connected again. ");
                                remoteDeviceConnectivityStatus = true;
                                onRemoteDeviceConnected();
                            }
                        });
                    }
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        monitorThread.start();
    }

    private void updateBanner(int status) {
        Animation animSlideDown = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
        Animation animSlideUp = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);

        if(status == BANNER_CONNECTED) {
            if(bannerView.getVisibility() == View.VISIBLE)
                bannerView.startAnimation(animSlideUp);

            bannerText.setText(R.string.connectedMessage);
            bannerView.setBackground(getDrawable(R.color.deciceConnected));
            bannerView.setVisibility(View.VISIBLE);
            bannerView.startAnimation(animSlideDown);

        } else if (status == BANNER_DISCONNECTED) {
            if(bannerView.getVisibility() == View.VISIBLE)
                bannerView.startAnimation(animSlideUp);

            bannerView.setBackground(getDrawable(R.color.deviceNotConnected));
            bannerView.setVisibility(View.VISIBLE);
            bannerText.setText(R.string.disconnectedMessage);
            bannerView.startAnimation(animSlideDown);

        } else if (status == BANNER_INVISIBLE) {
            bannerView.startAnimation(animSlideUp);
            bannerView.setVisibility(View.INVISIBLE);
        }
    }

    private void updateUI(RemoteDeviceData remoteDeviceData) {
        if(!motorButton.isEnabled()) {
            motorButton.setEnabled(true);
            motorButton.setBackground(getDrawable(R.drawable.button_shape));
        }
        DecimalFormat df = new DecimalFormat("00");
        String uptimeString = df.format(remoteDeviceData.getUptimeHour())+" : "+df.format(remoteDeviceData.getUptimeMinutes())+" : "+df.format(remoteDeviceData.getUptimeSeconds());
        currentWaterLevelText.setText(Integer.toString(remoteDeviceData.getWaterLevelPercentage()).concat(" %"));
        tankView.setProgress(remoteDeviceData.getWaterLevelPercentage());
        deviceUptimeText.setText(uptimeString);
        if(customDialog.isDialogVisible() && isMotorOn != remoteDeviceData.isMotorStatus())
            customDialog.hideDialogBox();
        isMotorOn = remoteDeviceData.isMotorStatus();
        motorButton.setText( (isMotorOn) ? "Turn Motor Off" : "Turn Motor On" );
    }

    public int gen() {
        Random r = new Random( System.currentTimeMillis() );
        return ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }

}