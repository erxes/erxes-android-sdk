package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ListenerService;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationListActivity extends AppCompatActivity  implements ErxesObserver {

    static private String TAG="ConversationListActivity";
    static public boolean chat_is_going = false;

    private RecyclerView supporterView;
    private TextView welcometext,date;
    private ViewPager viewpager;
    private ViewGroup info_header,container;
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
                         Log.d(TAG,"here changed");
//                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                     case ReturnType.INTEGRATION_CHANGED:
//                        info_header.setBackgroundColor(config.colorCode);
//                        addnew_conversation.getBackground().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
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

//        if(recyclerView!=null)
//            if(recyclerView.getAdapter()!=null){
//
//                ((ConversationListAdapter)recyclerView.getAdapter()).update_position(config.conversationId);
////                recyclerView.getAdapter().notifyDataSetChanged();
////                recyclerView.invalidate();
//
//            }
        config.conversationId = null;
        erxesRequest.getIntegration();
//        LayerDrawable layerDrawable = (LayerDrawable) getResources()
//                .getDrawable(R.drawable.pattern_color);
//        GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable
//                .findDrawableByLayerId(R.id.background);
//        gradientDrawable.setColor(config.colorCode);
//        info_header.setBackground(layerDrawable);
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
        viewpager = findViewById(R.id.viewpager);
        info_header = findViewById(R.id.info_header);
        container = findViewById(R.id.container);
        welcometext = findViewById(R.id.welcometext);
        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);
        date.setText(config.now());
//        welcometext.setText(config.messengerdata.messages.greetings.message);

        supporterView.setAdapter(new SupportAdapter(this));
        LinearLayoutManager supManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        Helper.display_configure(this,container,"#66000000");

        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewpager.setAdapter(tabAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewpager));
        viewpager.addOnPageChangeListener( new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);

//        viewpager.addOnPageChangeListener(tabLayout.TabLayoutOnPageChangeListener(tabLayout));
//        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewpager));

        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);
        erxesRequest.getFAQ();
    }
    public void start_new_conversation(View v){
        config.conversationId = null;
        Intent a = new Intent(this,MessageActivity.class);
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
