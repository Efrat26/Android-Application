package com.example.efiso.ex4ap2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by efiso on 18/06/2018.
 */

public class ImageHandler {
    private File [] pics;
    private List<byte[]> picsAsBytes;
    public ImageHandler(){
        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if(dcim != null){
            pics = dcim.listFiles();
            System.out.println("yay");
        }
    }
    public void CovertToBitMapPics(){
        this.picsAsBytes = new ArrayList<>();
        for (File pic:pics){
            try {
                FileInputStream fis = new FileInputStream(pic);
                Bitmap bm = BitmapFactory.decodeStream(fis);
                picsAsBytes.add(getBytesFromBitmap(bm));
            } catch (Exception e){

            }


        }

    }
    public byte[] getBytesFromBitmap(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,70,stream);
        return stream.toByteArray();
    }
}
