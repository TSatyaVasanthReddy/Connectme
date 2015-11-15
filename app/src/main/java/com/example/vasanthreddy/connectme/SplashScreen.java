package com.example.vasanthreddy.connectme;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Tapan on 12/10/15.
 */
public class SplashScreen extends  Activity {

    private  static int TIME_OUT = 2000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splashactivityxml);

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        Intent f = new Intent(SplashScreen.this, MapsActivity.class);
                        startActivity(f);

                        //overridePendingTransition(R.layout.activityfadein , R.layout.activityfadeout);

                    }
                }

                , TIME_OUT);

        setContentView(R.layout.splashactivityxml);

        }
}
