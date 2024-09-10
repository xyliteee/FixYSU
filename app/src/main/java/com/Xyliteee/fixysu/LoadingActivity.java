package com.Xyliteee.fixysu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {
    private Device[] devicesList;
    private SharedPreferences sharedPreferences;

    @Override
    public void onBackPressed() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        sharedPreferences = this.getSharedPreferences("loginPrefs", MODE_PRIVATE);
        Intent intent = getIntent();
        boolean autoLogin = intent.getBooleanExtra("autoLogin",false);
        if(autoLogin){AutoGetDeviceList();}
        else {GetDevicesList();}

    }

    private  void AutoGetDeviceList(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String inputCookieValue = sharedPreferences.getString("cookieValue","");
            final Device[] devices = LoginFunctions.AutoGetDevicesList(inputCookieValue);
            handler.post(() -> {
                devicesList = devices;
                Intent intent = new Intent(LoadingActivity.this, DeviceListShow.class);
                intent.putExtra("devicesList",devicesList);
                startActivity(intent);
            });
        });
    }

    public void GetDevicesList(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            final Device[] devices = LoginFunctions.GetDevicesList();
            handler.post(() -> {
                devicesList = devices;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cookieValue", LoginFunctions.cookieValue);
                editor.apply();
                Intent intent = new Intent(LoadingActivity.this, DeviceListShow.class);
                intent.putExtra("devicesList",devicesList);
                startActivity(intent);
            });
        });
    }
}