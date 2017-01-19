package com.upstack.solution.forecastweather;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar widget_refresh_pb = (ProgressBar) findViewById(R.id.widget_refresh_pb);
        widget_refresh_pb.getIndeterminateDrawable().setColorFilter(0xffFCDF55, android.graphics.PorterDuff.Mode.MULTIPLY);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, 2000);


    }
}
