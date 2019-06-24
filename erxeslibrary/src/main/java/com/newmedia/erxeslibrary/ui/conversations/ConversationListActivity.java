package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.ui.conversations.adapter.SupportAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.TabAdapter;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

public class ConversationListActivity extends AppCompatActivity implements ErxesObserver {

    static private String TAG = "ConversationListActivity";
    static public boolean chat_is_going = false;

    private RecyclerView supporterView;
    private TextView welcometext, date, title;
    private ViewPager viewpager;
    private ViewGroup info_header, container;
    private Config config;
    private ErxesRequest erxesRequest;
    private DataManager dataManager;
    private TabAdapter tabAdapter;
    public boolean isFirstStart = false;

    @Override
    public void notify(final int returnType, final String conversationId, String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturnType.Subscription:
                    case ReturnType.Getconversation:
                        Log.d(TAG, "here changed");
//                        recyclerView.getAdapter().notifyDataSetChanged();
                        break;
                    case ReturnType.INTEGRATION_CHANGED:
//                        info_header.setBackgroundColor(config.colorCode);
//                        addnew_conversation.getBackground().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
                        break;
                    case ReturnType.CONNECTIONFAILED:
                        break;
                    case ReturnType.LOGIN_SUCCESS:
                        init();
                        break;
                    case ReturnType.SERVERERROR:
                        break;
                    case ReturnType.LEAD:
                        if (tabAdapter != null)
                            ((SupportFragment) tabAdapter.getItem(0)).setLead();
                        break;
                    default:
                        break;
                }
                ;


            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        erxesRequest.remove(this);
    }

    private void startActivity() {
        this.finish();
        Intent a = new Intent(ConversationListActivity.this, ErxesActivity.class);
        startActivity(a);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        if (config.customerId == null) {
            startActivity();
            return;
        }
        if (TextUtils.isEmpty(dataManager.getDataS(DataManager.email)) &&
                !TextUtils.isEmpty(dataManager.getDataS(DataManager.phone))) {
            dataManager.setData(DataManager.customerId, null);
            startActivity();
            return;
        } else if (!TextUtils.isEmpty(dataManager.getDataS(DataManager.email)))
            erxesRequest.setConnect(dataManager.getDataS(DataManager.email), null, false,false);
        else erxesRequest.setConnect(null, dataManager.getDataS(DataManager.phone), false,false);

        erxesRequest.add(this);
        config.conversationId = null;

        dataManager.setData("chat_is_going", true);

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
        chat_is_going = false;
        dataManager.setData("chat_is_going", false);
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
        title = findViewById(R.id.title);
        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);

        dataManager = DataManager.getInstance(this);

        supporterView.setAdapter(new SupportAdapter(this));
        LinearLayoutManager supManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        Helper.display_configure(this, container, "#66000000");

        tabAdapter = new TabAdapter(getSupportFragmentManager(), this);
        TabLayout tabLayout = findViewById(R.id.tabs);
        viewpager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);

        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);
        erxesRequest.getFAQ();
    }

    private void init() {
        date.setText(config.now());
        if (config.messengerdata.getMessages() != null && config.messengerdata.getMessages().getGreetings() != null) {
            title.setText(config.messengerdata.getMessages().getGreetings().getTitle());
            welcometext.setText(config.messengerdata.getMessages().getGreetings().getMessage());
        }
    }

    public void start_new_conversation(View v) {
        config.conversationId = null;
        Intent a = new Intent(this, MessageActivity.class);
        startActivity(a);
    }

    public void logout(View v) {
        finish();
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(final View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundResource(R.drawable.action_background);
                    }
                });
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                ConversationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        v.setBackgroundColor(Color.parseColor("#00000000"));
                        if (v.getId() == R.id.logout)
                            logout(null);
                    }
                });
            }
            return true;
        }
    };
}
