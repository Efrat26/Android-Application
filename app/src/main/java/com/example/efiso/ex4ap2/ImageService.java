package com.example.efiso.ex4ap2;

import android.app.Service;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by efiso on 16/06/2018.
 */

public class ImageService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(){
            public void run(){
                File pic = null;
                int imgbyte = 0;
                try {
                    InetAddress serverAddress = InetAddress.getByName("10.0.2.2");
                    Socket socket = new Socket(serverAddress, 8888);
                    try {
                        OutputStream output = socket.getOutputStream();
                        FileInputStream fis = new FileInputStream(pic);
                        output.write(imgbyte);
                        output.flush();

                    } catch (Exception e) {
                        Log.e("TCP", "S Error", e);

                    } finally {
                        socket.close();
                    }
                } catch (Exception e) {
                    Log.e("TCP", "C: Error", e);
                }
            }
        };

        thread.start();

        Toast.makeText(this, getResources().getString(R.string.service_started),
                Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, getResources().getString(R.string.service_destroy),
                Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ConnectToService extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... urls) {
            File pic = null;
            int imgbyte = 0;
            try {
                InetAddress serverAddress = InetAddress.getByName("10.0.2.2");
                Socket socket = new Socket(serverAddress, 8888);
                try {
                    OutputStream output = socket.getOutputStream();
                    FileInputStream fis = new FileInputStream(pic);
                    output.write(imgbyte);
                    output.flush();

                } catch (Exception e) {
                    Log.e("TCP", "S Error", e);

                } finally {
                    socket.close();
                }
            } catch (Exception e) {
                Log.e("TCP", "C: Error", e);
            }
            return "hello";
        }

        protected void onPostExecute(int result) {
            // mImageView.setImageBitmap(result);
        }
    }
}
