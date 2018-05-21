package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ListenerService;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    private RecyclerView recyclerView;

    static private String TAG="ConversationListActivity";
    @Override
    public void notify(boolean status,final String conversationId) {
        if(status){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,""+conversationId);
                    recyclerView.getAdapter().notifyDataSetChanged();

                }
            });
        }
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

        if(recyclerView!=null)
            if(recyclerView.getAdapter()!=null){

                ((ConversationListAdapter)recyclerView.getAdapter()).update_position(Config.conversationId);
//                recyclerView.getAdapter().notifyDataSetChanged();
//                recyclerView.invalidate();

            }
        Config.conversationId = null;

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        Toolbar myToolbar =  findViewById(R.id.my_toolbar);
        if(Config.color!=null)
        myToolbar.setBackgroundColor(Color.parseColor(Config.color));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(null);




        recyclerView = this.findViewById(R.id.chat_recycler_view);
        this.findViewById(R.id.toolbar_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.conversationId = null;
                Intent a = new Intent(ConversationListActivity.this,MessageActivity.class);
                startActivity(a);

            }
        });
        this.findViewById(R.id.toolbar_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.Logout();
                Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
                startActivity(a);
                finish();


            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerView.setAdapter(
                new ConversationListAdapter(ConversationListActivity.this));



        Config.getConversations();

        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.conversation,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int i = item.getItemId();
//       if(i ==R.id.logout){
//
//            Config.Logout();
//            Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
//            startActivity(a);
//            finish();
//            return true;
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
