package com.newmedia.erxeslibrary.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ProgressRequestBody;
import com.newmedia.erxeslibrary.FileInfo;
import com.newmedia.erxeslibrary.R;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GFilePart implements ProgressRequestBody.Listener {
    static final String TAG = GFilePart.class.getName();
    File file;
    private FileInfo fileInfo;
    private Config config;
    private MessageActivity AC;
    private ProgressBar progressBar;
    private ViewGroup container,filelist,button_chatbox_send;
    private CircularProgressDrawable senddrawable;
    private List<JSONObject> uploadJsons = new ArrayList<>();

    public GFilePart(Config config,MessageActivity messageActivity) {
        this.AC = messageActivity;
        this.config = config;
        senddrawable = new CircularProgressDrawable(AC);
        senddrawable.setStrokeWidth(  5f);
        senddrawable.setCenterRadius(  30f);

        progressBar = AC.findViewById(R.id.simpleProgressBar);
        progressBar.getProgressDrawable().mutate().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_IN);
        progressBar.setMax(100);
        button_chatbox_send = AC.findViewById(R.id.button_chatbox_send);
        filelist = AC.findViewById(R.id.filelist);
        container = AC.findViewById(R.id.container);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public void test() {
        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(2,TimeUnit.MINUTES)
                .readTimeout(2,TimeUnit.MINUTES).build();
        Log.d(TAG,"type "+fileInfo.type);
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileInfo.name, RequestBody.create(MediaType.parse(fileInfo.type), getFile()))
                .addFormDataPart("name", fileInfo.name )
                .build();

        ;
        Request request = new Request.Builder()
                .url(config.HOST_UPLOAD)
                .addHeader("Authorization","")
                .post(new ProgressRequestBody(formBody,this)).build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("erxes_api", "failed" );

                e.printStackTrace();
                AC.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        button_chatbox_send.setClickable(true);
                        senddrawable.stop();
                        progressBar.setProgress(0);
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {

                    fileInfo.filepath = response.body().string();
                    uploadJsons.add(fileInfo.get());
                    Log.i("erxes_api", "upload complete");
                    AC.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater layoutInflater = LayoutInflater.from(AC);
                            View view = layoutInflater.inflate(R.layout.upload_file, filelist, false);
                            TextView filename = view.findViewById(R.id.filename);
                            view.findViewById(R.id.remove).setTag(uploadJsons.get(uploadJsons.size()-1));
                            view.findViewById(R.id.remove).setOnClickListener(remove_fun);
                            filename.setText(""+fileInfo.name);
                            filelist.addView(view);
                            progressBar.setProgress(0);
                        }
                    });
                }
                else{
                    AC.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
                AC.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senddrawable.stop();
                        button_chatbox_send.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });

                Log.i("erxes_api", "upload false");
            }
        });
    }
    private View.OnClickListener remove_fun = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            JSONObject tag = (JSONObject ) v.getTag();
            int index = uploadJsons.indexOf(tag);
            Log.d("myfo","index "+index);
            uploadJsons.remove(index);
            filelist.removeViewAt(index);

        }
    };
    public void upload()  {
        if(!config.isNetworkConnected()) {
            Snackbar.make(container,R.string.cantconnect,Snackbar.LENGTH_SHORT).show();
            return;
        }
        senddrawable.start();
        button_chatbox_send.setBackgroundDrawable(senddrawable);
        progressBar.setVisibility(View.VISIBLE);
        button_chatbox_send.setClickable(false);
        test();
    }
    public void end_of(){
        uploadJsons.clear();
        filelist.removeAllViews();
    }
    public List<JSONObject>  get(){
        return (uploadJsons.size() > 0) ? uploadJsons : null;
    }
    public void ActivityResult(int requestCode, int resultCode, Intent resultData){
        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {

            Uri returnUri = resultData.getData();

            fileInfo = new FileInfo(AC,returnUri);

            if (returnUri != null && "content".equals(returnUri.getScheme())) {
                fileInfo.init();
            } else {
                fileInfo.filepath = returnUri.getPath();
            }

            file = fileInfo.if_not_exist_create_file();
            if(file != null){
                Log.i("erxes_api", "result1");
                upload();
            }
            else{
                Log.i("erxes_api", "result3");
                Snackbar.make(container, R.string.fileerror, Snackbar.LENGTH_SHORT).show();
            }
        }
        Log.i("erxes_api", "result2"+requestCode+" ?"+resultCode+" "+Activity.RESULT_OK);

    }

    File getFile() {
        return file;
    }
    @Override
    public void onProgress(int progress) {
        progressBar.setProgress(progress);
    }

}

