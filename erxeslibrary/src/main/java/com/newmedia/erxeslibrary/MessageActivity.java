package com.newmedia.erxeslibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErrorType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Configuration.ListenerService;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class MessageActivity extends AppCompatActivity implements ErxesObserver {



    //    private List<Message> mMessageList;
    private Button button_chatbox_send;
    private EditText edittext_chatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm = Realm.getDefaultInstance();
    private ImageView profileImage,isMessenOnlineImage;
    private TextView fullname;
    private CircularProgressDrawable senddrawable;
    private ViewGroup container;
    private final String TAG="MESSAGEACTIVITY";
    @Override
    public void notify(boolean status,String conversationId,ErrorType errorType) {
        if(status){

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageListAdapter adapter = (MessageListAdapter)mMessageRecycler.getAdapter();
                    if((Config.welcomeMessage!=null && adapter.getItemCount() == 1)||adapter.getItemCount() == 0){
                        RealmResults<ConversationMessage> d = null;

                        d = realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).findAll();
                        adapter.setmMessageList(d);
                        adapter.notifyDataSetChanged();

                        if(d.size()>1)
                            mMessageRecycler.smoothScrollToPosition(d.size()-1);
                        Intent intent2 = new Intent(MessageActivity.this, ListenerService.class);
                        startService(intent2);
                    }else {

                        if(adapter.getItemCount() > 2 && adapter.refresh_data())
                            mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                ///header image fix
                    ConversationMessage lastmessage = realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).isNotNull("user").sort("createdAt", Sort.DESCENDING).findFirst();
                    if(lastmessage!=null )
                    {
                        String myString = lastmessage.getUser().fullName;
                        String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                        fullname.setText(upperString);
                        GlideApp.with(MessageActivity.this).load(lastmessage.getUser().avatar).placeholder(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(profileImage);

                    }

                }
            });
        }
        else{
            if(errorType !=null){
                if(errorType == ErrorType.SERVERERROR)
                {
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                            return;
                        }});
                }
                else if(errorType == ErrorType.CONNECTIONFAILED){

                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                            return;
                        }});
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messege);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.getWindow().setAttributes(lp);
        Toolbar toolbar =  findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
//
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        container = this.findViewById(R.id.container);
        button_chatbox_send = this.findViewById(R.id.button_chatbox_send);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profileImage = this.findViewById(R.id.profile_image);
        isMessenOnlineImage = this.findViewById(R.id.isonline);
        fullname = this.findViewById(R.id.fullname);
        senddrawable = new CircularProgressDrawable(this);
        Log.d(TAG,"color"+Config.color);
        if(Config.color!=null) {

            this.findViewById(R.id.oneline).setBackgroundColor(Color.parseColor(Config.color));
            button_chatbox_send.setTextColor(Color.parseColor(Config.color));
            toolbar.setBackgroundColor(Color.parseColor(Config.color));
            this.findViewById(R.id.attach).getBackground()
                    .setColorFilter(Color.parseColor(Config.color), PorterDuff.Mode.SRC_ATOP);
        }
        button_chatbox_send.setOnClickListener(onClickListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }

        });
        edittext_chatbox = this.findViewById(R.id.edittext_chatbox);


        mMessageRecycler =  findViewById(R.id.reyclerview_message_list);
        if(Config.wallpaper!=null)
        if(Config.wallpaper.equalsIgnoreCase("1"))
            mMessageRecycler.setBackgroundResource(R.drawable.bitmap1);
        else if(Config.wallpaper.equalsIgnoreCase("2"))
            mMessageRecycler.setBackgroundResource(R.drawable.bitmap2);
        else if(Config.wallpaper.equalsIgnoreCase("3"))
            mMessageRecycler.setBackgroundResource(R.drawable.bitmap3);
        else if(Config.wallpaper.equalsIgnoreCase("4"))
            mMessageRecycler.setBackgroundResource(R.drawable.bitmap4);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mMessageRecycler.setLayoutManager(linearLayoutManager);


        if(!Config.isMessengerOnline)
            isMessenOnlineImage.setVisibility(View.GONE);


        if(Config.conversationId != null) {
            Conversation conversation = realm.where(Conversation.class).equalTo("_id",Config.conversationId).findFirst();
            realm.beginTransaction();
            conversation.setIsread(true);
            realm.insertOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();


            RealmResults<ConversationMessage> d = realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).findAll();
            ConversationMessage lastmessage =realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).isNotNull("user").sort("createdAt", Sort.DESCENDING).findFirst();
            if(lastmessage!=null )
            {
                GlideApp.with(this).load(lastmessage.getUser().avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
                String myString = lastmessage.getUser().fullName;
                String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
                fullname.setText(upperString);
            }
            mMessageRecycler.setAdapter(new MessageListAdapter(this,d));
            linearLayoutManager.setStackFromEnd(true);
//            if(d.size()>2) {
//                linearLayoutManager.smoothScrollToPosition(d.size() - 1);
//                linearLayoutManager.scrollToPosition(d.size());
//                mMessageRecycler.smoothScrollBy(0,mMessageRecycler.g);
//            }
            ErxesRequest.getMessages(Config.conversationId);
        }
        else{
            mMessageRecycler.setAdapter(new MessageListAdapter(this,new ArrayList<ConversationMessage>()));
        }
        if (shouldAskPermissions()) {
            askPermissions();
        }
        senddrawable.setStrokeWidth(  5f);
        senddrawable.setCenterRadius(  30f);



    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!Config.isNetworkConnected()) {
                Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                return;
            }
            if(!edittext_chatbox.getText().toString().equalsIgnoreCase("")) {
                List<JSONObject> temp = null;
                if(jsonObject!=null)
                {
                    temp = new ArrayList<>();
                    try
                    {
                        temp.add(new JSONObject(jsonObject.toString()));
                        jsonObject = null;
                    } catch (JSONException e) {
                        temp = null;
                        e.printStackTrace();
                    }
                }
                if (Config.conversationId != null) {
                    ErxesRequest.InsertMessage(edittext_chatbox.getText().toString(), Config.conversationId, temp);
                    edittext_chatbox.setText("");
                } else {
                    ErxesRequest.InsertNewMessage(edittext_chatbox.getText().toString(), temp);
                    edittext_chatbox.setText("");
                }
            }

        }
    };
    void refreshItems() {
        ErxesRequest.getMessages(Config.conversationId);

    }
    @Override
    protected void onPause() {
        super.onPause();
        ErxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ErxesRequest.add(this);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.conversation,menu);
//        return true;
//    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //Android 4.4 (API level 19)
    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_PICK );
//        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 444);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {
            Uri returnUri = resultData.getData();

            filepath = null;


            //Now fetch the new URI

//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Log.d("erxes_api", "above from kitkat"+resultData.getData().toString());
//                try {
//                    getContentResolver().openInputStream(returnUri);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            else
                {
                if (returnUri != null && "content".equals(returnUri.getScheme())) {
                    Cursor cursor = this.getContentResolver().query(returnUri, new String[]
                            {MediaStore.Images.ImageColumns.DATA,
                                    MediaStore.Images.ImageColumns.SIZE,
                                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                                    MediaStore.Images.ImageColumns.MIME_TYPE
                            }, null, null, null);

                    cursor.moveToFirst();

                    filepath = cursor.getString(0);
                    size = cursor.getString(1);
                    name = cursor.getString(2);
                    type = cursor.getString(3);
//                    Log.d("erxes_api", "info = " + cursor.getString(0) + " " + cursor.getString(1) + " ?" + cursor.getString(2) + cursor.getString(3));
                    cursor.close();
                } else {
                    filepath = returnUri.getPath();
                }

                if(filepath == null) {
                    File root = android.os.Environment.getExternalStorageDirectory();
                    File tempFile = new File(root.getAbsolutePath()+"/Download", "temp_image");
                    FileOutputStream outputStream =null;
                    try {
                        tempFile.createNewFile();
                        //                this.getContentResolver().openInputStream(returnUri,new FileOutputStream(tempFile));
                        outputStream = new FileOutputStream(tempFile);
                    } catch ( IOException e) {
                        e.printStackTrace();
                        Log.d("erxes_api", "create file" );
                    }
                    if(outputStream != null)
                    try{
                        InputStream inputStream = this.getContentResolver().openInputStream(returnUri);


                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                        outputStream.close();
                        upload(tempFile);

                    } catch ( IOException e) {
                        e.printStackTrace();
                        Log.d("erxes_api", "output stream error" );
                        Snackbar.make(container, R.string.fileerror, Snackbar.LENGTH_SHORT).show();
                    }
                }
                else
                    upload(new File(filepath));
            }

        }
    }




    public void upload( File file)  {

        senddrawable.start();
        button_chatbox_send.setBackgroundDrawable(senddrawable);

        button_chatbox_send.setClickable(false);
        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(3,TimeUnit.MINUTES)
                .readTimeout(3,TimeUnit.MINUTES).build();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", name, RequestBody.create(MediaType.parse(type), file))
                .addFormDataPart("name", name )
                .build();

        Request request = new Request.Builder().url(Config.HOST_UPLOAD).addHeader("Authorization","")
                .post(formBody).build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("erxes_api", "failed" );

                button_chatbox_send.setClickable(true);
                e.printStackTrace();
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senddrawable.stop();
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
//                    Log.i("erxes_api", "yes" + response.body().string());
                    filepath = response.body().string();
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("type",type);
                        jsonObject.put("size",size);
                        jsonObject.put("name",name);
                        jsonObject.put("url",filepath);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("erxes_api", "upload complete");


                }
                else{
                    MessageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
                button_chatbox_send.setClickable(true);
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senddrawable.stop();
                    }
                });
                Log.i("erxes_api", "upload false");

            }
        });
    }
    String filepath,size,name,type,attachments;
    JSONObject jsonObject;
    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


}
