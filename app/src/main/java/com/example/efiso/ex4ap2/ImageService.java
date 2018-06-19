package com.example.efiso.ex4ap2;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

/**
 * Created by efiso on 16/06/2018.
 */

public class ImageService extends Service {
    private int alreadySent;
    final IntentFilter theFilter = new IntentFilter();
    private static final int SERVER_PORT = 9000;
    private static final String SERVER_IP = "10.0.2.2";
    private List<byte[]> imageAsByte;
    TcpClient client;
    ImageHandler imgHandler;
    private BroadcastReceiver reciever;

    @Override
    public void onCreate() {
        super.onCreate();
        this.alreadySent = 0;
        client = new TcpClient();
        new Thread(client).start();
        imgHandler = new ImageHandler(client.getSocket());
        this.imageAsByte = imgHandler.getImageBytesList();
        theFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        theFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo netinfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(netinfo != null && alreadySent == 0){
                    if(netinfo.getType() == ConnectivityManager.TYPE_WIFI && alreadySent == 0){
                        if(netinfo.getState() == NetworkInfo.State.CONNECTED && alreadySent == 0){
                            alreadySent = 1;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    imgHandler.sendImage(imageAsByte);
                                }
                            }).start();
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.reciever, theFilter);
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
        private Socket socket;
        ByteArrayOutputStream outToServer;
        boolean connected;

        @Override
        public void run() {
            connected = false;
            boolean scanning = true;
            int numberOfTry = 0;
            while (scanning && numberOfTry < 2) {
                numberOfTry++;
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    socket = new Socket(serverAddr, SERVER_PORT);
                    scanning = false;
                    connected = true;

                } catch (IOException e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }

            }
        }
        public Socket getSocket() {
            while (!connected) {}
            return socket;
        }

    }
}