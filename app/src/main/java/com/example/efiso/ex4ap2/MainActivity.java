package com.example.efiso.ex4ap2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startService(View v)
    {
        Intent service = new Intent(this,ImageService.class);
        startService(service);
    }

    public void stopService(View v)
    {
        Intent service = new Intent(this,ImageService.class);
        stopService(service);
        //finish();
    }
}
