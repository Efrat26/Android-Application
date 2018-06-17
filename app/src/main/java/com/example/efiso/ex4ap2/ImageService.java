package com.example.efiso.ex4ap2;

import android.app.Service;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by efiso on 16/06/2018.
 */

public class ImageService extends Service {
    private Socket socket;
    private static final int SERVERPORT = 9000;
    private static final String SERVER_IP = "10.0.2.2";

    @Override
    public void onCreate() {
        super.onCreate();
        socket = null;
        new Thread(new TcpClient()).start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

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

    class TcpClient implements Runnable {
        @Override
        public void run() {
            boolean scanning = true;
            int numberOfTry = 0;
            while (scanning && numberOfTry < 2) {
                numberOfTry++;
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVERPORT);
                    scanning = false;
                    try{
                        OutputStream output = socket.getOutputStream();
                        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
                        outToServer.writeBytes("hello from android" + '\n');
                        outToServer.flush();
                    } catch (Exception e){
                        System.out.println(e);
                    }
                } catch (IOException e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }

            }
        }
    }
}