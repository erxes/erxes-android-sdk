package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ListenerService;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    private RecyclerView recyclerView;

    static private String TAG="ConversationListActivity";
    private ViewGroup addnew_conversation;
    private ViewGroup info_header,container;
    static public boolean chat_is_going = false;

    @Override
    public void notify(final ReturnType returnType, final String conversationId, String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType){
                    case subscription:
                    case getconversation:
                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    case INTEGRATION_CHANGED:
                        info_header.setBackgroundColor(Config.colorCode);
                        addnew_conversation.getBackground().setColorFilter(Config.colorCode, PorterDuff.Mode.SRC_ATOP);
                        break;
                    case CONNECTIONFAILED:
                        break;
                    case SERVERERROR:
                        break;

                        default:break;
                };


            }
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        ErxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Config.customerId == null) {
            logout(null);
            return;
        }
        ErxesRequest.add(this);

        if(recyclerView!=null)
            if(recyclerView.getAdapter()!=null){

                ((ConversationListAdapter)recyclerView.getAdapter()).update_position(Config.conversationId);
//                recyclerView.getAdapter().notifyDataSetChanged();
//                recyclerView.invalidate();

            }
        Config.conversationId = null;
        ErxesRequest.getIntegration(Config.brandCode);
        info_header.setBackgroundColor(Config.colorCode);
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



        setContentView(R.layout.activity_conversation);

        addnew_conversation = findViewById(R.id.newconversation);
        info_header = findViewById(R.id.info_header);
        container = findViewById(R.id.container);


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = (int)( size.y *0.8);

        getWindow().setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);


        container.getLayoutParams().height = height;
        container.requestLayout();

        addnew_conversation.getBackground().setColorFilter(Config.colorCode, PorterDuff.Mode.SRC_ATOP);


        recyclerView = this.findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        recyclerView.setAdapter(
                new ConversationListAdapter(ConversationListActivity.this));

        ErxesRequest.getConversations();
        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);


    }
    public void start_new_conversation(View v){
        Config.conversationId = null;
        Intent a = new Intent(ConversationListActivity.this,MessageActivity.class);
        startActivity(a);
    }


    public void logout(View v){
        Log.d("myfo","logout conversation");
        Config.Logout();
        Intent a = new Intent(ConversationListActivity.this,ErxesActivity.class);
        startActivity(a);
        finish();
    }
}
