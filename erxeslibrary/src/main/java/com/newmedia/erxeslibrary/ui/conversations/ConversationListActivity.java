package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ListenerService;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    private RecyclerView recyclerView;

    static private String TAG="ConversationListActivity";
    private ViewGroup addnew_conversation;
    private ViewGroup info_header,container;
    static public boolean chat_is_going = false;
    private Config config;
    private ErxesRequest erxesRequest;
    @Override
    public void notify(final int  returnType, final String conversationId, String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType){
                    case ReturnType.Subscription:
                     case ReturnType.Getconversation:
                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                     case ReturnType.INTEGRATION_CHANGED:
                        info_header.setBackgroundColor(config.colorCode);
                        addnew_conversation.getBackground().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
                        break;
                    case ReturnType.CONNECTIONFAILED:
                        break;
                    case ReturnType.SERVERERROR:
                        break;

                        default:break;
                };


            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();

        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        if(config.customerId == null) {
            this.finish();
            Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
            startActivity(a);
            return;
        }
        erxesRequest.add(this);

        if(recyclerView!=null)
            if(recyclerView.getAdapter()!=null){

                ((ConversationListAdapter)recyclerView.getAdapter()).update_position(config.conversationId);
//                recyclerView.getAdapter().notifyDataSetChanged();
//                recyclerView.invalidate();

            }
        config.conversationId = null;
        erxesRequest.getIntegration();
        info_header.setBackgroundColor(config.colorCode);
        chat_is_going = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chat_is_going =false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        setContentView(R.layout.activity_conversation);

        addnew_conversation = findViewById(R.id.newconversation);
        info_header = findViewById(R.id.info_header);
        container = findViewById(R.id.container);
        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        this.findViewById(R.id.start).setOnTouchListener(touchListener);

//        ((TextView)this.findViewById(R.id.dp)).setText(""+getResources().getDisplayMetrics().density);

        Helper.display_configure(this,container,"#66000000");

        addnew_conversation.getBackground().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);


        recyclerView = this.findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
//        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        ConversationListAdapter adapter=new ConversationListAdapter(this);
        recyclerView.setAdapter(adapter);
        if( 0 == adapter.conversationList.size() ){
            start_new_conversation(null);
        }
//        erxesRequest.getConversations();
        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);



    }
    public void start_new_conversation(View v){
        config.conversationId = null;
        Intent a = new Intent(ConversationListActivity.this,MessageActivity.class);
        startActivity(a);
    }


    public void logout(View v){
        finish();
    }
    private View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            }
            else if(event.getAction() == MotionEvent.ACTION_UP){
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundColor(Color.parseColor("#00000000"));
                        if(v.getId() == R.id.logout)
                            logout(null);
                        else if(v.getId() == R.id.start)
                            start_new_conversation(null);
                    }
                });
            }
            return true;
        }
    };
}
