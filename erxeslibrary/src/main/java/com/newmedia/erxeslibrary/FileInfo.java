package com.newmedia.erxeslibrary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInfo {
    public String filepath = null,size,name,type,attachments;
    private Uri returnUri;
    private Context context;
    public FileInfo(Context context, Uri returnUri) {
        this.context = context;
        this.returnUri = returnUri;
    }

    public JSONObject get(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type",type);
            jsonObject.put("size",size);
            jsonObject.put("name",name);
            jsonObject.put("url",filepath);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void init(){
        Cursor cursor = context.getContentResolver().query(returnUri, new String[]
                {       MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.SIZE,
                        MediaStore.Images.ImageColumns.DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.MIME_TYPE
                }, null, null, null);

        cursor.moveToFirst();

        this.filepath = cursor.getString(0);
        this.size = cursor.getString(1);
        this.name = cursor.getString(2);
        this.type = cursor.getString(3);
        cursor.close();
    }
    public File if_not_exist_create_file(){
        if(this.filepath == null) {
            File root = android.os.Environment.getExternalStorageDirectory();
            File tempFile = new File(root.getAbsolutePath()+"/Download", "temp_image");
            FileOutputStream outputStream =null;
            try {
                tempFile.createNewFile();
                //this.getContentResolver().openInputStream(returnUri,new FileOutputStream(tempFile));
                outputStream = new FileOutputStream(tempFile);
            } catch ( IOException e) {
                e.printStackTrace();
                Log.d("erxes_api", "cant create file" );
            }
            if(outputStream != null)
                try{
                    InputStream inputStream = context.getContentResolver().openInputStream(returnUri);
                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();
                    return tempFile;
                } catch ( IOException e) {
                    e.printStackTrace();
                    Log.d("erxes_api", "output stream error" );
                    return null;

                }
            return null;
        }
        else
            return new File(this.filepath);
    }
}
