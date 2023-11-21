package com.Xyliteee.fixysu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceListShow extends AppCompatActivity {
    public Device[] devicesList;
    private LoginFunctions loginFunctions;
    private final ConstraintLayout[] deviceBoxes = new ConstraintLayout[4];
    private final TextView[] ipBoxes = new TextView[4];
    private final TextView[] nameBoxes = new TextView[4];
    private final TextView[] macBoxes = new TextView[4];
    private final TextView[] timeBoxes = new TextView[4];
    private final ImageView[] imageBoxes = new ImageView[4];
    private final Button[] buttonBoxes = new Button[4];
    private SwipeRefreshLayout refreshLayout;
    private SharedPreferences sharedPreferences;
    private boolean messageFlag = true;
    private long mExitTime = 0;
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(DeviceListShow.this, "再次操作以返回登陆界面", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
        else{
        Intent intent = new Intent(DeviceListShow.this, MainActivity.class);
        intent.putExtra("backFromOtherPages",true);
        startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list_show);
        Intent intent = getIntent();
        InitWidgets();
        devicesList = (Device[]) intent.getSerializableExtra("devicesList");
        sharedPreferences = this.getSharedPreferences("showMessageFlag", MODE_PRIVATE);
        messageFlag = sharedPreferences.getBoolean("messageFlag",true);

        if (devicesList[0].deviceName.equals("无设备")){
            Intent intent2 = new Intent(DeviceListShow.this, nodevice.class);
            startActivity(intent2);
        }
        else if(devicesList[0].deviceName.equals("登陆失败")){
            Toast.makeText(DeviceListShow.this, "登录失败，请重新登录", Toast.LENGTH_SHORT).show();
            Intent intent2 = new Intent(DeviceListShow.this, MainActivity.class);
            intent2.putExtra("backFromOtherPages",true);
            startActivity(intent2);
        }
        else{
            if(devicesList.length > 4){
                Device[] p = new Device[4];
                System.arraycopy(devicesList, 0, p, 0, 4);
                devicesList = p;
            }
                int deviceNumbers = devicesList.length;
                loginFunctions = LoginFunctions.getInstance();
                for(int i = 0;i<deviceNumbers;i++){
                    ipBoxes[i].setText(devicesList[i].ipAddress);
                    nameBoxes[i].setText(devicesList[i].deviceName);
                    macBoxes[i].setText(devicesList[i].macAddress);
                    timeBoxes[i].setText(devicesList[i].lastOnlineTime);
                    if (devicesList[i].deviceName.contains("电脑")){imageBoxes[i].setImageResource(R.drawable.computer_show);}
                    else if (devicesList[i].deviceName.contains("安卓")) {imageBoxes[i].setImageResource(R.drawable.phone_show);}
                    else{imageBoxes[i].setImageResource(R.drawable.phone_show);}
                    deviceBoxes[i].setVisibility(View.VISIBLE);
                }
            }
        ShowMessage();
    }

    private void ShowMessage(){
        if (messageFlag){
            new AlertDialog.Builder(this)
                    .setTitle("提示说明")
                    .setMessage("学校网站现在不能很准确识别设备型号，因此建议使用登陆时间判断具体是哪个设备，如果你会使用MAC地址也行")
                    .setPositiveButton("不再提示", (dialog, which) -> {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("messageFlag",false);
                        editor.apply();
                    })
                    .setNegativeButton("确定", null)
                    .show();
        }
    }
    private void Downline(String currentIP){
        new AlertDialog.Builder(this)
                .setTitle("消息提示")
                .setMessage("你确定要下线这个设备吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        loginFunctions.KickDevice(currentIP);
                        handler.post(() -> {
                            Intent intent = new Intent(DeviceListShow.this, LoadingActivity.class);
                            startActivity(intent);
                        });
                    });

                })
                .setNegativeButton("取消", null)
                .show();
    }
    private void KickDevice0(View view){
        String currentIP = devicesList[0].ipAddress;
        Downline(currentIP);

    }
    private void KickDevice1(View view){
        String currentIP = devicesList[1].ipAddress;
        Downline(currentIP);

    }
    private void KickDevice2(View view){
        String currentIP = devicesList[2].ipAddress;
        Downline(currentIP);

    }
    private void KickDevice3(View view){
        String currentIP = devicesList[3].ipAddress;
        Downline(currentIP);

    }


    private void InitWidgets(){
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            Intent intent = new Intent(DeviceListShow.this, LoadingActivity.class);
            startActivity(intent);
            refreshLayout.setRefreshing(false);
        });
        deviceBoxes[0] = findViewById(R.id.DeviceBox0);
        deviceBoxes[1] = findViewById(R.id.DeviceBox1);
        deviceBoxes[2] = findViewById(R.id.DeviceBox2);
        deviceBoxes[3] = findViewById(R.id.DeviceBox3);

        ipBoxes[0] = findViewById(R.id.IPBox0);
        ipBoxes[1] = findViewById(R.id.IPBox1);
        ipBoxes[2] =  findViewById(R.id.IPBox2);
        ipBoxes[3] =  findViewById(R.id.IPBox3);

        nameBoxes[0] = findViewById(R.id.NameBox0);
        nameBoxes[1] =  findViewById(R.id.NameBox1);
        nameBoxes[2] = findViewById(R.id.NameBox2);
        nameBoxes[3] =  findViewById(R.id.NameBox3);

        macBoxes[0] =  findViewById(R.id.MacBox0);
        macBoxes[1] =  findViewById(R.id.MacBox1);
        macBoxes[2] =  findViewById(R.id.MacBox2);
        macBoxes[3] =  findViewById(R.id.MacBox3);

        timeBoxes[0] =  findViewById(R.id.TimeBox0);
        timeBoxes[1] =  findViewById(R.id.TimeBox1);
        timeBoxes[2] =  findViewById(R.id.TimeBox2);
        timeBoxes[3] =  findViewById(R.id.TimeBox3);

        imageBoxes[0] =  findViewById(R.id.Image0);
        imageBoxes[1] =  findViewById(R.id.Image1);
        imageBoxes[2] =  findViewById(R.id.Image2);
        imageBoxes[3] =  findViewById(R.id.Image3);

        buttonBoxes[0] =  findViewById(R.id.Button0);
        buttonBoxes[1] =  findViewById(R.id.Button1);
        buttonBoxes[2] =  findViewById(R.id.Button2);
        buttonBoxes[3] =  findViewById(R.id.Button3);
        buttonBoxes[0].setOnClickListener(this::KickDevice0);
        buttonBoxes[1].setOnClickListener(this::KickDevice1);
        buttonBoxes[2].setOnClickListener(this::KickDevice2);
        buttonBoxes[3].setOnClickListener(this::KickDevice3);
        for (int i=0;i<4;i++){
            deviceBoxes[i].setVisibility(View.INVISIBLE);
        }
    }
}
