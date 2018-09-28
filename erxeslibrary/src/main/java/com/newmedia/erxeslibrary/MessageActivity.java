package com.newmedia.erxeslibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
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
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErxesRealmModule;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ProgressRequestBody;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
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
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity implements ErxesObserver,ProgressRequestBody.Listener {



    //    private List<Message> mMessageList;
    private ViewGroup button_chatbox_send;
    private EditText edittext_chatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm ;
    private ImageView profile1,profile2;
    private TextView names,isMessenOnlineImage;
    private CircularProgressDrawable senddrawable;
    private ViewGroup container,linearlayout,filelist,upload_group;
    private List<JSONObject> upload_files = new ArrayList<>();
    private ProgressBar progressBar;
    private Config config;
    private ErxesRequest erxesRequest;
    Point size;
    FIleInfo fIleInfo;
    //    private ImageView uploadImage;
    private final String TAG="MESSAGEACTIVITY";
    @Override
    public void notify(final int returnType, String conversationId,  String message) {


            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageListAdapter adapter = (MessageListAdapter)mMessageRecycler.getAdapter();
                    switch (returnType){
                        case ReturnType.Subscription:

                            header_profile_change();
                            isMessenOnlineImage.setText(R.string.online);
                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                            //without break
                        case ReturnType.Getmessages:
                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.Mutation:

                            if(adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);

                            upload_files.clear();
                            filelist.removeAllViews();
                            break;
                        case ReturnType.Mutation_new:
                            RealmResults<ConversationMessage> d = null;

                            d = realm.where(ConversationMessage.class).equalTo("conversationId",config.conversationId).findAll();
                            adapter.setmMessageList(d);
                            adapter.notifyDataSetChanged();

                            if(d.size()>1)
                                mMessageRecycler.smoothScrollToPosition(d.size()-1);
                            Intent intent2 = new Intent(MessageActivity.this, ListenerService.class);
                            startService(intent2);
                            upload_files.clear();
                            filelist.removeAllViews();
                            break;
                        case ReturnType.IsMessengerOnline:
                            header_profile_change();
                            break;

                        case ReturnType.SERVERERROR:
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.CONNECTIONFAILED:
                            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                            break;
                        case ReturnType.GetSupporters:
                            header_profile_change();
                            break;
                    }
                }
            });


    }
    void header_profile_change(){
        RealmResults<User> users =  realm.where(User.class).findAll();
        if(users.size() == 0){
            erxesRequest.getSupporters();
        }
        else {
            isMessenOnlineImage.setVisibility(View.VISIBLE);
        }
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
        isMessenOnlineImage.setText(config.messenger_status_check()?R.string.online:R.string.offline);
        progressBar.getProgressDrawable().mutate().setColorFilter(config.colorCode,PorterDuff.Mode.SRC_IN);
//        isMessenOnlineImage.setVisibility(
//                (Config.isNetworkConnected()&&Config.IsMessengerOnline) ?View.VISIBLE:View.INVISIBLE);

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
                        container.getLayoutParams().height= WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });

            }
        });
        upload_group = this.findViewById(R.id.upload_group);
        button_chatbox_send = this.findViewById(R.id.button_chatbox_send);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profile1 = this.findViewById(R.id.profile1);
        profile2 = this.findViewById(R.id.profile2);
        filelist = this.findViewById(R.id.filelist);
        progressBar = this.findViewById(R.id.simpleProgressBar);
        progressBar.setMax(100);

        isMessenOnlineImage = this.findViewById(R.id.isOnline);
        names = this.findViewById(R.id.names);

        senddrawable = new CircularProgressDrawable(this);

        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }

        });

        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        this.findViewById(R.id.back).setOnTouchListener(touchListener);
        edittext_chatbox = this.findViewById(R.id.edittext_chatbox);


        mMessageRecycler =  findViewById(R.id.reyclerview_message_list);
        if(config.wallpaper!=null)
            if(config.wallpaper.equalsIgnoreCase("1"))
                mMessageRecycler.setBackgroundResource(R.drawable.bitmap1);
            else if(config.wallpaper.equalsIgnoreCase("2"))
                mMessageRecycler.setBackgroundResource(R.drawable.bitmap2);
            else if(config.wallpaper.equalsIgnoreCase("3"))
                mMessageRecycler.setBackgroundResource(R.drawable.bitmap3);
            else if(config.wallpaper.equalsIgnoreCase("4"))
                mMessageRecycler.setBackgroundResource(R.drawable.bitmap4);


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        realm = Realm.getInstance(Helper.getRealmConfig());
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messege);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        load_findViewByid();
        mMessageRecycler.setLayoutManager(linearLayoutManager);


        if(config.conversationId != null) {
            Conversation conversation = realm.where(Conversation.class).equalTo("_id",config.conversationId).findFirst();
            realm.beginTransaction();
            conversation.isread = true;
            realm.insertOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();

            RealmResults<ConversationMessage> d =
                    realm.where(ConversationMessage.class).
                            equalTo("conversationId",config.conversationId).findAll();
            mMessageRecycler.setAdapter(new MessageListAdapter(this,d));
            linearLayoutManager.setStackFromEnd(true);

            erxesRequest.getMessages(config.conversationId);
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
        config.Logout();
        realm.beginTransaction();
        realm.delete(Conversation.class);
        realm.delete(ConversationMessage.class);
        realm.commitTransaction();
        finish();
    }
    public void send_message(View view) {
        Log.d("myfo","clicked");
        if(!config.isNetworkConnected()) {
            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(!edittext_chatbox.getText().toString().equalsIgnoreCase("")) {
            List<JSONObject> temp = null;
            if(upload_files.size() > 0) {

                temp = upload_files;
//                temp.add(upload_files.toString());
                Log.d("myfo","data "+temp.toString());
            }


            if (config.conversationId != null) {
                erxesRequest.InsertMessage(edittext_chatbox.getText().toString(), config.conversationId, temp);
                edittext_chatbox.setText("");
            } else {
                erxesRequest.InsertNewMessage(edittext_chatbox.getText().toString(), temp);
                edittext_chatbox.setText("");
            }

        }

    };
    public void refreshItems() {
        if(config.conversationId!=null) {
            erxesRequest.getMessages(config.conversationId);
        }
        else
            swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
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
        upload_group.setClickable(false);
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
        upload_group.setClickable(true);

    }

    public void upload(final File file)  {
        if(!config.isNetworkConnected()) {
            Snackbar.make(container,R.string.cantconnect,Snackbar.LENGTH_SHORT).show();
            return;
        }
        senddrawable.start();
//        button_chatbox_send.setImageResource(R.drawable.file);
        button_chatbox_send.setBackgroundDrawable(senddrawable);
        progressBar.setVisibility(View.VISIBLE);

        button_chatbox_send.setClickable(false);
        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(2,TimeUnit.MINUTES)
                .readTimeout(2,TimeUnit.MINUTES).build();
        RequestBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fIleInfo.name, RequestBody.create(MediaType.parse(fIleInfo.type), file))
                .addFormDataPart("name", fIleInfo.name )
                .build();

        ;
        Request request = new Request.Builder().url(config.HOST_UPLOAD).addHeader("Authorization","")
                .post(new ProgressRequestBody(formBody,this)).build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("erxes_api", "failed" );

                e.printStackTrace();
                MessageActivity.this.runOnUiThread(new Runnable() {
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

                    fIleInfo.filepath = response.body().string();
                    upload_files.add(fIleInfo.get());
                    Log.i("erxes_api", "upload complete");
                    MessageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LayoutInflater layoutInflater = LayoutInflater.from(MessageActivity.this);
                            View view = layoutInflater.inflate(R.layout.upload_file, filelist, false);
                            TextView filename = view.findViewById(R.id.filename);
                            view.findViewById(R.id.remove).setTag(upload_files.get(upload_files.size()-1));
                            view.findViewById(R.id.remove).setOnClickListener(remove_fun);
                            filename.setText(""+fIleInfo.name);
                            filelist.addView(view);
                            progressBar.setProgress(0);
                        }
                    });
                }
                else{
                    MessageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                            Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
                MessageActivity.this.runOnUiThread(new Runnable() {
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
            int index = upload_files.indexOf(tag);
            Log.d("myfo","index "+index);
            upload_files.remove(index);
            filelist.removeViewAt(index);

        }
    };
    @Override
    public void onProgress(int progress) {

        progressBar.setProgress(progress);
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


    private View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundColor(Color.parseColor("#00000000"));
                        if(v.getId() == R.id.logout)
                            logout(null);
                        else if(v.getId() == R.id.back)
                            Click_back(null);
                    }
                });
            }
            return true;
        }
    };



}
