package com.example.waterlevelindicator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    private ImageView   dropletImage;
    private TextView    projectText, neeramText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = SplashScreen.this.getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(SplashScreen.this , R.color.White));
        }

        dropletImage = findViewById(R.id.dropletImage);
        projectText = findViewById(R.id.project_text);
        neeramText = findViewById(R.id.neeram_text);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, 3000);

        Animation fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_animation);
        dropletImage.startAnimation(fadeAnimation);
        neeramText.startAnimation(fadeAnimation);
        projectText.startAnimation(fadeAnimation);

    }
}