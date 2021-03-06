package com.example.efiso.ex4ap2;

import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by efiso on 18/06/2018.
 */
 /*
   a class that is responsible for sending the images
 */
public class ImageHandler {

    private boolean finished;
    private List<File> picsFilesAsList;
    private List<byte[]> picsAsBytes;
    private List<String> picsNames;
    private Socket socket;
    OutputStream out;
    DataOutputStream dos;
    private  int alreadySent;
    /*
       opens a data stream & initializes lists
    */
    public ImageHandler(Socket s){
        this.alreadySent = 0;
        this.socket = s;
        try {
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);
        }catch (Exception e){}
        this.picsNames = new ArrayList<>();
        this.picsFilesAsList = new ArrayList<>();
        //String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() ;
       // this.ListAllFiles(path);
    }
    /*
       converts images to a bitmap pictures
    */
    public void CovertToBitMapPics(){
        finished = false;
        this.picsAsBytes = new ArrayList<>();
        for (File pic:picsFilesAsList){
            try {
                FileInputStream fis = new FileInputStream(pic);
                Bitmap bm = BitmapFactory.decodeStream(fis);
                picsAsBytes.add(getBytesFromBitmap(bm));
            } catch (Exception e){

            }


        }
        finished = true;

    }
    /*
       gets the bytes from the image
    */
    public byte[] getBytesFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,70,stream);
        return stream.toByteArray();
    }
    /*
       returns the list with pictures with bytes
    */
    public List<byte[]> getImageBytesList(){
        while(!finished){};
        return this.picsAsBytes;
    }
    /*
       calls send bytes to send the image bytes
    */
    public void sendBytes(byte[] myByteArray, int index) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length, index);
    }
    /*
       sends to the service the images. before sending the image it sends "begin" and the length
       of the image. after sending the image it sends "end" and the name of the image.
    */
    public void sendBytes(byte[] myByteArray, int start, int len, int index) throws IOException {
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        // Other checks if needed.

        // May be better to save the streams in the support class;
        // just like the socket variable.

        //String numAsInt = Integer.toString(len);
        //dos.write(numAsInt.getBytes(Charset.forName("UTF-8")));
        dos.writeUTF("begin"+Integer.toString(len));
        dos.flush();
        if (len > 0) {
            dos.write(myByteArray, start, len);
            dos.flush();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dos.writeUTF("end" + this.picsNames.get(index));
        dos.flush();
    }
    /*
       gets the already send variable
    */
    public int AlreadySent(){
        return this.alreadySent;
    }
    /*
       sets the already send variable
    */
    public void setAlreadySent(int val){
        this.alreadySent = val;
    }
    /*
       list all the files in the DCIM recursively
    */
    public void ListAllFiles(String path, boolean isRecursiveCall) {
        // Get all the files from a directory.
        //File[] fList = directory.listFiles();
        if(picsNames != null && !isRecursiveCall){
            picsNames.clear();
        }
        if(picsFilesAsList !=null && !isRecursiveCall){
            picsFilesAsList.clear();
        }
        File [] pics;
        File dcim = new File(path);
        if(dcim != null){
            pics = dcim.listFiles();
            for (File pic:pics){

                if (pic.isFile()) {
                    this.picsNames.add(pic.getName());
                    this.picsFilesAsList.add(pic);
                    //this.picsNames.add(pic);
                } else if (pic.isDirectory()) {
                    ListAllFiles(pic.getAbsolutePath(), true);
                }
            }
            //System.out.println("yay");
            CovertToBitMapPics();
        }

    }
    /*
       gets the file's extension
    */
    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
}