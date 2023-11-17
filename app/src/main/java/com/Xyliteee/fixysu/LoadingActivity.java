package com.Xyliteee.fixysu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {
    private Device[] devicesList;
    private LoginFunctions loginFunctions;

    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        loginFunctions = LoginFunctions.getInstance();
        GetDevicesList();

    }

    public void GetDevicesList(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            final Device[]devices = loginFunctions.GetDevicesList();
            handler.post(() -> {
                devicesList = devices;
                Intent intent = new Intent(LoadingActivity.this, DeviceListShow.class);
                intent.putExtra("devicesList",devicesList);
                startActivity(intent);
            });
        });
    }
}