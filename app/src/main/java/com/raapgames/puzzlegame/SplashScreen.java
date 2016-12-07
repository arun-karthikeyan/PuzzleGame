package com.raapgames.puzzlegame;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        ImageView imageView = (ImageView) findViewById(R.id.logo);
        int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels-10;
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        Log.d("SplashScreen","width::"+width);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(width,height);
        rp.topMargin = 0;
        rp.leftMargin = 0;
        imageView.setLayoutParams(rp);
        CountDownTimer timer = new CountDownTimer(5000, 1000) {
            Toast mToast = null;

            @Override
            public void onTick(long millisUntilFinished) {
                if (mToast != null) mToast.cancel();
                mToast = Toast.makeText(getApplicationContext(), "Redirecting in " + millisUntilFinished / 1000 + " secs", Toast.LENGTH_SHORT);
                mToast.show();
            }

            @Override
            public void onFinish() {
                if (mToast != null) mToast.cancel();
                startActivity(new Intent(SplashScreen.this, HomeScreenActivity.class));
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

    }
}
