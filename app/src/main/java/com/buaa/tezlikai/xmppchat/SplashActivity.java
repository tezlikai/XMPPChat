package com.buaa.tezlikai.xmppchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;

import com.buaa.tezlikai.xmppchat.activity.LoginActivity;
import com.buaa.tezlikai.xmppchat.utils.ThreadUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //停留3秒，跳入登录界面
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                //停留3秒
                SystemClock.sleep(3000);

                //进入登录界面
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
