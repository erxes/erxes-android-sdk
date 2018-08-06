package com.newmedia.erxeslibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Configuration.ListenerService;
import com.newmedia.erxeslibrary.Configuration.SoftKeyboard;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.Model.User;


import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

public class MessageActivity extends AppCompatActivity implements ErxesObserver {



    //    private List<Message> mMessageList;
    private ImageView button_chatbox_send;
    private EditText edittext_chatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm = Realm.getDefaultInstance();
    private ImageView profile1,profile2;
    private TextView names,isMessenOnlineImage;
    private CircularProgressDrawable senddrawable;
    private ViewGroup container,linearlayout;
    Point size;
    FIleInfo fIleInfo;
//    private ImageView uploadImage;
    private final String TAG="MESSAGEACTIVITY";
    @Override
    public void notify(final ReturnType returnType, String conversationId,  String message) {


            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageListAdapter adapter = (MessageListAdapter)mMessageRecycler.getAdapter();
                    switch (returnType){
                        case subscription:

                            header_profile_change();
                            isMessenOnlineImage.setText(R.string.online);
                            //without break
                        case getmessages:
                        case Mutation:

                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);

                            break;
                        case Mutation_new:
                            RealmResults<ConversationMessage> d = null;

                            d = realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).findAll();
                            adapter.setmMessageList(d);
                            adapter.notifyDataSetChanged();

                            if(d.size()>1)
                                mMessageRecycler.smoothScrollToPosition(d.size()-1);
                            Intent intent2 = new Intent(MessageActivity.this, ListenerService.class);
                            startService(intent2);
                            
                            break;
                        case isMessengerOnline:
                            header_profile_change();
                            break;

                        case SERVERERROR:
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case CONNECTIONFAILED:
                            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                    }
                }
            });


    }
    void header_profile_change(){
        RealmResults<User> users =  realm.where(User.class).findAll();
        if(users.size()>0){
            if(users.get(0).avatar!=null)
                GlideApp.with(this).load(users.get(0).avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profile1);

            profile1.setVisibility(View.VISIBLE);
            String myString = users.get(0).fullName;
            String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
            names.setText(upperString);
            names.setVisibility(View.VISIBLE);
        }
        else {
            profile1.setVisibility(View.INVISIBLE);
            names.setVisibility(View.INVISIBLE);
        }
        if(users.size()>1){
            if(users.get(1).avatar!=null)
                GlideApp.with(this).load(users.get(1).avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into((ImageView)this.findViewById(R.id.profile2));
            profile2.setVisibility(View.VISIBLE);
            String myString = users.get(1).fullName;
            String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
            names.setText(names.getText().toString()+", "+upperString);
        }
        else {
            profile2.setVisibility(View.INVISIBLE);
        }
        isMessenOnlineImage.setText(Config.messenger_status_check()?R.string.online:R.string.offline);

//        isMessenOnlineImage.setVisibility(
//                (Config.isNetworkConnected()&&Config.isMessengerOnline) ?View.VISIBLE:View.INVISIBLE);
    }
    void load_findViewByid(){
        // for dialog size
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.getWindow().setAttributes(lp);

        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);


        getWindow().setLayout(size.x, WindowManager.LayoutParams.MATCH_PARENT);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);



        // load views
        container = this.findViewById(R.id.container);
        linearlayout = this.findViewById(R.id.linearlayout);
        container.getLayoutParams().height = size.y * 8 / 10; /// 80% ondortoi
        container.requestLayout();

        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard(linearlayout, im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged()
        {

            @Override
            public void onSoftKeyboardHide()
            {
                // Code here
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("myfo","hide");
                        container.getLayoutParams().height =size.y*8/10;
                        container.requestLayout();
                    }
                });

            }

            @Override
            public void onSoftKeyboardShow()
            {
                // Code here
                // Code here
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("myfo","show");
                        container.getLayoutParams().height= WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });

            }
        });
//        uploadImage = this.findViewById(R.id.uploadImage);
        button_chatbox_send = this.findViewById(R.id.button_chatbox_send);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profile1 = this.findViewById(R.id.profile1);
        profile2 = this.findViewById(R.id.profile2);

        isMessenOnlineImage = this.findViewById(R.id.isOnline);
        names = this.findViewById(R.id.names);

        senddrawable = new CircularProgressDrawable(this);

        this.findViewById(R.id.info_header).setBackgroundColor(Config.colorCode);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messege);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        load_findViewByid();
        mMessageRecycler.setLayoutManager(linearLayoutManager);


        if(Config.conversationId != null) {
            Conversation conversation = realm.where(Conversation.class).equalTo("_id",Config.conversationId).findFirst();
            realm.beginTransaction();
            conversation.isread = true;
            realm.insertOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();

            RealmResults<ConversationMessage> d =
                    realm.where(ConversationMessage.class).
                            equalTo("conversationId",Config.conversationId).findAll();
            mMessageRecycler.setAdapter(new MessageListAdapter(this,d));
            linearLayoutManager.setStackFromEnd(true);

            ErxesRequest.getMessages(Config.conversationId);
        }
        else {
            mMessageRecycler.setAdapter(new MessageListAdapter(this,new ArrayList<ConversationMessage>()));
        }

        header_profile_change();

        if (shouldAskPermissions()) {
            askPermissions();
        }
        senddrawable.setStrokeWidth(  5f);
        senddrawable.setCenterRadius(  30f);



    }

    public void Click_back(View v){
        finish();
    }
    public void logout(View v){
        Config.Logout();
        finish();
    }
    public void send_message(View view) {
        if(!Config.isNetworkConnected()) {
            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(!edittext_chatbox.getText().toString().equalsIgnoreCase("")) {
            List<JSONObject> temp = null;
            if(fIleInfo!=null && fIleInfo.get()!=null)
            {
                temp = new ArrayList<>();
                temp.add(fIleInfo.get());
                fIleInfo = null;

            }
            if (Config.conversationId != null) {
                ErxesRequest.InsertMessage(edittext_chatbox.getText().toString(), Config.conversationId, temp);
                edittext_chatbox.setText("");
            } else {
                ErxesRequest.InsertNewMessage(edittext_chatbox.getText().toString(), temp);
                edittext_chatbox.setText("");
            }
        }

    };
    public void refreshItems() {
        if(Config.conversationId!=null) {
            ErxesRequest.getMessages(Config.conversationId);
        }
        else
            swipeRefreshLayout.setRefreshing(false);

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
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == 444 && resultCode == Activity.RESULT_OK) {
            Uri returnUri = resultData.getData();
            fIleInfo = new FIleInfo();
            fIleInfo.filepath = null;


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
                            {       MediaStore.Images.ImageColumns.DATA,
                                    MediaStore.Images.ImageColumns.SIZE,
                                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                                    MediaStore.Images.ImageColumns.MIME_TYPE
                            }, null, null, null);

                    cursor.moveToFirst();
                    fIleInfo = new FIleInfo();
                    fIleInfo.filepath =  cursor.getString(0);
                    fIleInfo.size = cursor.getString(1);
                    fIleInfo.name = cursor.getString(2);
                    fIleInfo.type = cursor.getString(3);

//                    Log.d("erxes_api", "info = " + cursor.getString(0) + " " + cursor.getString(1) + " ?" + cursor.getString(2) + cursor.getString(3));
                    cursor.close();
                } else {
                    fIleInfo.filepath = returnUri.getPath();
                }

                if(fIleInfo.filepath == null) {
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
                    upload(new File(fIleInfo.filepath));
            }

        }
    }

    public void upload( File file)  {
        if(!Config.isNetworkConnected()) {
            Snackbar.make(container,R.string.cantconnect,Snackbar.LENGTH_SHORT).show();
            return;
        }
        senddrawable.start();
//        uploadImage.setImageResource(R.drawable.file);
//        uploadImage.setBackgroundDrawable(senddrawable);

        button_chatbox_send.setClickable(false);
        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(3,TimeUnit.MINUTES)
                .readTimeout(3,TimeUnit.MINUTES).build();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fIleInfo.name, RequestBody.create(MediaType.parse(fIleInfo.type), file))
                .addFormDataPart("name", fIleInfo.name )
                .build();

        Request request = new Request.Builder().url(Config.HOST_UPLOAD).addHeader("Authorization","")
                .post(formBody).build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("erxes_api", "failed" );

                e.printStackTrace();
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_chatbox_send.setClickable(true);
                        senddrawable.stop();
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
//                    Log.i("erxes_api", "yes" + response.body().string());
                    fIleInfo.filepath = response.body().string();
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

                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senddrawable.stop();
                        button_chatbox_send.setClickable(true);
                    }
                });
                Log.i("erxes_api", "upload false");
            }

        });
    }

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
