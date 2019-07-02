package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.newmedia.erxeslibrary.CustomViewPager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.ui.conversations.adapter.SupportAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.TabAdapter;
import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

public class ConversationListActivity extends AppCompatActivity implements ErxesObserver {

    static private String TAG = "ConversationListActivity";
    static public boolean chat_is_going = false;

    private RecyclerView supporterView;
    private TextView welcometext, date, title;
    private CustomViewPager viewpager;
    private ViewGroup info_header, container;
    private Config config;
    private ErxesRequest erxesRequest;
    private DataManager dataManager;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    public boolean isFirstStart = false;
    private ImageView fb, tw, yt;

    @Override
    public void notify(final int returnType, final String conversationId, final String message) {
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
                        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
                        break;
                    case ReturnType.LEAD:
                        if (tabAdapter != null)
                            ((SupportFragment) tabAdapter.getItem(0)).setLead();
                        break;
                    case ReturnType.FAQ:
                        if (tabLayout != null) {
                            tabLayout.setVisibility(View.VISIBLE);
                            if (tabAdapter != null)
                                ((FaqFragment) tabAdapter.getItem(1)).init();
                        }
                        break;
                    case ReturnType.savedLead:
                        if (tabAdapter != null)
                            ((SupportFragment) tabAdapter.getItem(0)).setLeadAgain();
                        break;
                    default:
                        break;
                }
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

    public void sendLead() {
        erxesRequest.sendLead();
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        erxesRequest.add(this);
        if (config.customerId == null) {
            startActivity();
            return;
        }
        if (TextUtils.isEmpty(dataManager.getDataS(DataManager.email)) &&
                TextUtils.isEmpty(dataManager.getDataS(DataManager.phone))) {
            dataManager.setData(DataManager.customerId, null);
            startActivity();
            return;
        } else if (dataManager.getDataB(DataManager.isUser)) {
            erxesRequest.setConnect(
                    dataManager.getDataS(DataManager.email),
                    dataManager.getDataS(DataManager.phone),
                    true,
                    false,
                    dataManager.getDataS(DataManager.customData));
        } else if (!TextUtils.isEmpty(dataManager.getDataS(DataManager.email)))
            erxesRequest.setConnect(
                    dataManager.getDataS(DataManager.email), null, false, false, null
            );
        else
            erxesRequest.setConnect(null, dataManager.getDataS(DataManager.phone), false, false, null);

        erxesRequest.getGEO();
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
        welcometext = findViewById(R.id.greetingMessage);
        title = findViewById(R.id.greetingTitle);
        this.findViewById(R.id.logout).setOnTouchListener(touchListener);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);
        fb = findViewById(R.id.fb);
        tw = findViewById(R.id.tw);
        yt = findViewById(R.id.yt);
        tabLayout = findViewById(R.id.tabs);

        dataManager = DataManager.getInstance(this);

        viewpager.setPagingEnabled(false);
        tabAdapter = new TabAdapter(getSupportFragmentManager(), this);

        viewpager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);

        if (getIntent().getBooleanExtra("isFromLogin", false)) {
            init();
        }

        supporterView.setAdapter(new SupportAdapter(this));
        LinearLayoutManager supManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        Helper.display_configure(this, container, "#66000000");


        Intent intent2 = new Intent(this, ListenerService.class);
        startService(intent2);
        erxesRequest.getSupporters();

        fb.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        tw.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        yt.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        this.findViewById(R.id.fbcontainer).setOnTouchListener(touchListener);
        this.findViewById(R.id.twcontainer).setOnTouchListener(touchListener);
        this.findViewById(R.id.ytcontainer).setOnTouchListener(touchListener);
    }

    private void init() {
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null)
            erxesRequest.getFAQ();
        date.setText(config.now());
        if (config.messengerdata.getMessages() != null && config.messengerdata.getMessages().getGreetings() != null) {
            title.setText(config.messengerdata.getMessages().getGreetings().getTitle());
            welcometext.setText(config.messengerdata.getMessages().getGreetings().getMessage());
        }
        if (config.messengerdata.getFacebook() != null && config.messengerdata.getFacebook().startsWith("http"))
            this.findViewById(R.id.fbcontainer).setVisibility(View.VISIBLE);
        else this.findViewById(R.id.fbcontainer).setVisibility(View.GONE);

        if (config.messengerdata.getTwitter() != null && config.messengerdata.getTwitter().startsWith("http"))
            this.findViewById(R.id.twcontainer).setVisibility(View.VISIBLE);
        else this.findViewById(R.id.twcontainer).setVisibility(View.GONE);

        if (config.messengerdata.getYoutube() != null && config.messengerdata.getYoutube().startsWith("http"))
            this.findViewById(R.id.ytcontainer).setVisibility(View.VISIBLE);
        else this.findViewById(R.id.ytcontainer).setVisibility(View.GONE);
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
                        if (v.getId() == R.id.logout) {
                            logout(null);
                        } else if (v.getId() == R.id.fbcontainer) {
                            if (config.messengerdata.getFacebook() != null && config.messengerdata.getFacebook().contains("http")) {
                                startFacebook(config.messengerdata.getFacebook());
                            }
                        } else if (v.getId() == R.id.twcontainer) {
                            if (config.messengerdata.getTwitter() != null && config.messengerdata.getTwitter().contains("http")) {
                                startTwitter(config.messengerdata.getTwitter());
                            }
                        } else if (v.getId() == R.id.ytcontainer) {
                            if (config.messengerdata.getYoutube() != null && config.messengerdata.getYoutube().contains("http")) {
                                startYoutube(config.messengerdata.getYoutube());
                            }
                        }
                    }
                });
            }
            return true;
        }
    };

    private void startYoutube(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void startFacebook(String facebookUrl) {

        try {
            Uri uri;

            int versionCode = getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0)
                    .versionCode;

            if (versionCode >= 3002850) {
                facebookUrl = facebookUrl.toLowerCase().replace("www.", "m.");
                if (!facebookUrl.startsWith("https")) {
                    facebookUrl = "https://" + facebookUrl;
                }
                uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
            } else {
                String pageID = facebookUrl.substring(facebookUrl.lastIndexOf("/"));

                uri = Uri.parse("fb://page" + pageID);
            }
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Throwable e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

    private void startTwitter(String url) {
        if (url.startsWith("https://twitter.com/")) {
            url = url.replace("https://twitter.com/", "");
        }
        String Username = url;
        try {
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + Username)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + Username)));
        }
    }
}
