package com.Xyliteee.fixysu;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class nodevice extends AppCompatActivity {
    private  long mExitTime = 0;
    private Button refreshPage ;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(nodevice.this, "再次操作以返回桌面", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        }
        else{
            this.finish();
            System.exit(0);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodevice);
        refreshPage = findViewById(R.id.ReFreshPage);
    }

    public void RefreshPage(View view){
        Intent intent = new Intent(nodevice.this, LoadingActivity.class);
        startActivity(intent);
    }
}