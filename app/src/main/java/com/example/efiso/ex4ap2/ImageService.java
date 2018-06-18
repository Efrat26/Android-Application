package com.example.efiso.ex4ap2;

import android.app.Service;
import android.content.Intent;
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
    private static final String start = "BEGIN";
    private static final String end = "END";
    private static final int SERVER_PORT = 9000;
    private static final String SERVER_IP = "10.0.2.2";
    private List<byte[]> imageAsByte;
    TcpClient client;
    ImageHandler imgHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new TcpClient();
        new Thread(client).start();
        imgHandler = new ImageHandler(client.getSocket());
        this.imageAsByte = imgHandler.getImageBytesList();
        new Thread(new Runnable() {
            @Override
            public void run() {
                imgHandler.sendImage(imageAsByte);
            }
        }).start();


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