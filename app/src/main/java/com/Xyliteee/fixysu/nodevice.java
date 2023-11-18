package com.Xyliteee.fixysu;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

public class nodevice extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(nodevice.this, MainActivity.class);
        intent.putExtra("backFromOtherPages",true);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodevice);
    }

    public void RefreshPage(View view){
        Intent intent = new Intent(nodevice.this, LoadingActivity.class);
        startActivity(intent);
    }
}