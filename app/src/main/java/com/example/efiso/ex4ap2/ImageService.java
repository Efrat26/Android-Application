package com.example.efiso.ex4ap2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
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
    public static final String NOTIFICATION_CHANNEL_ID = "1";
    @Override
    public void onCreate() {
        super.onCreate();
        final int notify_id=1;
        final NotificationManager NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
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
                            builder.setSmallIcon(R.drawable.ic_launcher_background);
                            builder.setContentTitle("Download Status");
                            builder.setContentText("Download in Progress");
                            builder.setPriority(NotificationCompat.PRIORITY_LOW);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    /*
                                    for(int icr = 0; icr<= 100; icr+=5) {
                                        builder.setProgress(100,icr,false);
                                        NM.notify(notify_id, builder.build());
                                        try {
                                            Thread.sleep(2000);
                                        } catch (Exception e){

                                        }
                                    }
                                    */
                                    builder.setProgress(0,0,false);
                                    builder.setContentText("Download Completed");
                                    NM.notify(1, builder.build());

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