package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ListenerService;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;


import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MessageActivity extends AppCompatActivity implements ErxesObserver {



    //    private List<Message> mMessageList;
    private Button button_chatbox_send;
    private EditText edittext_chatbox;
    private  RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Realm realm = Realm.getDefaultInstance();
    @Override
    public void notify(boolean status,String conversationId) {
        if(status){
            Log.d("erxes_api","notify message");
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

                }
            });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messege);
        Toolbar toolbar =  findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        if(Config.color!=null)
            toolbar.setBackgroundColor(Color.parseColor(Config.color));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        button_chatbox_send = this.findViewById(R.id.button_chatbox_send);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
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


        //daraa zasah




        if(Config.conversationId != null) {
            Conversation conversation = realm.where(Conversation.class).equalTo("_id",Config.conversationId).findFirst();
            realm.beginTransaction();
            conversation.setIsread(true);
            realm.insertOrUpdate(conversation);
            realm.commitTransaction();
            realm.close();


            RealmResults<ConversationMessage> d = realm.where(ConversationMessage.class).equalTo("conversationId",Config.conversationId).findAll();

            mMessageRecycler.setAdapter(new MessageListAdapter(d));
            if(d.size()>2)
                mMessageRecycler.smoothScrollToPosition(d.size()-1);
            Config.getMessages(Config.conversationId);
        }
        else{
            mMessageRecycler.setAdapter(new MessageListAdapter(new ArrayList<ConversationMessage>()));
        }


    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!edittext_chatbox.getText().toString().equalsIgnoreCase(""))
                if(Config.conversationId != null) {
                    Config.InsertMessage(edittext_chatbox.getText().toString(), Config.conversationId);
                    edittext_chatbox.setText("");
                }else{
                    Config.InsertNewMessage(edittext_chatbox.getText().toString());
                    edittext_chatbox.setText("");
                }

        }
    };
    void refreshItems() {
        Config.getMessages(Config.conversationId);

    }
    @Override
    protected void onPause() {
        super.onPause();
        Config.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.add(this);
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


}
