package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErrorType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ListenerService;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    private RecyclerView recyclerView;

    static private String TAG="ConversationListActivity";
    private Toolbar myToolbar;
    static public boolean chat_is_going = false;
    private FloatingActionButton fab;
    @Override
    public void notify(boolean status,final String conversationId,ErrorType errorType) {
        if(status){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyDataSetChanged();
                    if(Config.color!=null) {
//                        myToolbar.setBackgroundColor(Color.parseColor(Config.color));
                        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Config.color)));
                    }
                }
            });
        }
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

        if(recyclerView!=null)
            if(recyclerView.getAdapter()!=null){

                ((ConversationListAdapter)recyclerView.getAdapter()).update_position(Config.conversationId);
//                recyclerView.getAdapter().notifyDataSetChanged();
//                recyclerView.invalidate();

            }
        Config.conversationId = null;
        ErxesRequest.getIntegration(Config.brandCode);
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
        setContentView(R.layout.activity_delete);
        myToolbar =  findViewById(R.id.toolbar);



//        setSupportActionBar(null);


        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.getWindow().setAttributes(lp);


        fab =  findViewById(R.id.fab);
        if(Config.color!=null){
            Log.d(TAG,"toolbar color");
            myToolbar.setBackgroundColor(Color.parseColor(Config.color));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Config.color)));

        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.conversationId = null;
                Intent a = new Intent(ConversationListActivity.this,MessageActivity.class);
                startActivity(a);
            }
        });



        recyclerView = this.findViewById(R.id.chat_recycler_view);
//        this.findViewById(R.id.toolbar_add).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Config.conversationId = null;
//                Intent a = new Intent(ConversationListActivity.this,MessageActivity.class);
//                startActivity(a);
//
//            }
//        });
//        this.findViewById(R.id.toolbar_logout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Config.Logout();
//                Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
//                startActivity(a);
//                finish();
//
//
//            }
//        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerView.setAdapter(
                new ConversationListAdapter(ConversationListActivity.this));



        ErxesRequest.getConversations();

        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.conversation,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if(i == R.id.logout){

            Config.Logout();
            Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
            startActivity(a);
            finish();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
