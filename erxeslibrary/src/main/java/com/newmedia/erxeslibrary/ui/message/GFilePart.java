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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.erxes.io.opens.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.connection.helper.ProgressRequestBody;
import com.newmedia.erxeslibrary.helper.FileInfo;
import com.newmedia.erxeslibrary.R;

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
    private File file;
    private FileInfo fileInfo;
    private Config config;
    private MessageActivity messageActivity;
    private ProgressBar progressBar;
    private ViewGroup container,filelist, buttonChatboxSend;
    private CircularProgressDrawable senddrawable;
    private List<AttachmentInput> uploadJsons = new ArrayList<>();

    GFilePart(Config config, MessageActivity messageActivity) {
        this.messageActivity = messageActivity;
        this.config = config;
        senddrawable = new CircularProgressDrawable(this.messageActivity);
        senddrawable.setStrokeWidth(  5f);
        senddrawable.setCenterRadius(  30f);

        progressBar = this.messageActivity.findViewById(R.id.simpleProgressBar);
        progressBar.getProgressDrawable().mutate().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_IN);
        progressBar.setMax(100);
        buttonChatboxSend = this.messageActivity.findViewById(R.id.button_chatbox_send);
        filelist = this.messageActivity.findViewById(R.id.filelist);
        container = this.messageActivity.findViewById(R.id.container);
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

        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileInfo.name, RequestBody.create(MediaType.parse(fileInfo.type), getFile()))
                .addFormDataPart("name", fileInfo.name )
                .build();

        Request request = new Request.Builder()
                .url(config.hostUpload)
                .addHeader("Authorization","")
                .post(new ProgressRequestBody(formBody,this)).build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("erxes_api", "failed" );

                e.printStackTrace();
                messageActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        buttonChatboxSend.setClickable(true);
                        senddrawable.stop();
                        progressBar.setProgress(0);
                        Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        fileInfo.filepath = response.body().string();
                    }
                    uploadJsons.add(fileInfo.get());
                    messageActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater layoutInflater = LayoutInflater.from(messageActivity);
                            View view = layoutInflater.inflate(R.layout.upload_file, filelist, false);
                            View spaceView = view.findViewById(R.id.spaceView);
                            LinearLayout itemBackground = view.findViewById(R.id.itemBackground);
                            TextView filename = view.findViewById(R.id.filename);
                            ImageView imageView = view.findViewById(R.id.remove);
                            ImageView icon = view.findViewById(R.id.image_input);
                            itemBackground.setBackgroundColor(config.colorCode);
                            spaceView.setBackgroundColor(config.getInColor(config.colorCode));
                            filename.setTextColor(config.getInColor(config.colorCode));
                            icon.getDrawable()
                                    .setColorFilter(
                                            config.getInColor(config.colorCode),
                                            PorterDuff.Mode.SRC_ATOP
                                    );
                            Glide.with(config.context)
                                    .load(config.getCancelIcon(config.getInColor(config.colorCode)))
                                    .into(imageView);
                            imageView.setTag(uploadJsons.get(uploadJsons.size()-1));
                            imageView.setOnClickListener(removeFun);
                            filename.setText("" + fileInfo.name);
                            filelist.addView(view);
                            progressBar.setProgress(0);
                        }
                    });
                }
                else{
                    messageActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                            Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
                messageActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senddrawable.stop();
                        buttonChatboxSend.setClickable(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });

                Log.i("erxes_api", "upload false");
            }
        });
    }
    private View.OnClickListener removeFun = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AttachmentInput tag = (AttachmentInput) v.getTag();
            int index = uploadJsons.indexOf(tag);
            Log.d("myfo","index "+index);
            uploadJsons.remove(index);
            filelist.removeViewAt(index);

        }
    };
    public void upload()  {
        if(!config.isNetworkConnected()) {
            Snackbar.make(container,R.string.Failed,Snackbar.LENGTH_SHORT).show();
            return;
        }
        senddrawable.start();
        buttonChatboxSend.setBackgroundDrawable(senddrawable);
        progressBar.setVisibility(View.VISIBLE);
        buttonChatboxSend.setClickable(false);
        test();
    }
    void end_of(){
        uploadJsons.clear();
        filelist.removeAllViews();
    }
    public List<AttachmentInput>  get(){
        return (uploadJsons.size() > 0) ? uploadJsons : null;
    }
    void ActivityResult(int requestCode, int resultCode, Intent resultData){
        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {

            Uri returnUri = resultData.getData();

            fileInfo = new FileInfo(messageActivity,returnUri);

            if (returnUri != null && "content".equals(returnUri.getScheme())) {
                fileInfo.init();
            } else {
                if (returnUri != null) {
                    fileInfo.filepath = returnUri.getPath();
                }
            }

            file = fileInfo.if_not_exist_create_file();
            if(file != null){
                Log.i("erxes_api", "result1");
                upload();
            }
            else{
                Log.i("erxes_api", "result3");
                Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
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

