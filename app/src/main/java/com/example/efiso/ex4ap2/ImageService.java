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
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
    private NotificationChannel channel;
    NotificationManager notificationManager;
    NotificationManagerCompat notificationManagerCompat;
    NotificationCompat.Builder mBuilder;
    final int notificationID = (int)System.currentTimeMillis();
    @Override
    public void onCreate() {
        super.onCreate();
        this.createNotificationChannel();

        notificationManagerCompat = NotificationManagerCompat.from(this);
        mBuilder = new NotificationCompat.Builder(this, "1");
        mBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_LOW);
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
                            // Issue the initial notification with zero progress
                            final int PROGRESS_MAX = imageAsByte.size();
                            int PROGRESS_CURRENT = 0;
                            mBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                            notificationManager.notify(notificationID, mBuilder.build());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int counter = 0;
                                    if (imgHandler.AlreadySent() == 0) {
                                        for (byte[] picAsByte : imageAsByte) {
                                            try {
                                                // sendBytes(start.getBytes());
                                                ///Thread.sleep(2000);
                                                imgHandler.sendBytes(picAsByte, counter);
                                                Thread.sleep(2000);
                                                ++counter;
                                                mBuilder.setProgress(PROGRESS_MAX, counter, false);
                                                notificationManager.notify(notificationID, mBuilder.build());
                                                //sendBytes(end.getBytes());

                                            } catch (Exception e) {
                                                System.out.println(e);
                                            }
                                        }
                                        imgHandler.setAlreadySent(1);
                                    }
                                    imgHandler.sendImage(imageAsByte);
                                    mBuilder.setContentText("Download complete")
                                            .setProgress(0,0,false);
                                    notificationManager.notify(notificationID, mBuilder.build());

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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
             channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
             notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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