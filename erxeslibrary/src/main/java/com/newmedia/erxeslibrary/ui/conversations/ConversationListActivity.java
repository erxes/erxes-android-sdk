package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.newmedia.erxes.subscription.ConversationChangedSubscription;
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
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

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
    private ImageView fb, tw, yt, cancelImageView;
    private LinearLayout tabsContainer;
    private Intent intent;
    private List<String> disposabledChanged = new ArrayList<>();
    private CompositeDisposable disposablesChanged = new CompositeDisposable();
    private ApolloSubscriptionCall<ConversationChangedSubscription.Data> opensourceChangedCall;

    @Override
    public void notify(final int returnType, final String conversationId, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturnType.ComingNewMessage:
                        if (tabAdapter != null && ((SupportFragment) tabAdapter.getItem(0))
                                .recyclerView != null) {
                            ((SupportFragment) tabAdapter.getItem(0))
                                    .recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        break;
                    case ReturnType.Getconversation:
                        if (tabAdapter != null && ((SupportFragment) tabAdapter.getItem(0))
                                .recyclerView != null) {
                            ((SupportFragment) tabAdapter.getItem(0))
                                    .recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        if (config.conversations.size() > 0) {
                            stopService(intent);
                            startService(intent);
                            if (config.messengerdata.isForceLogoutWhenResolve()) {
                                initConversationChanged();
                            }
                        }
                        break;
                    case ReturnType.CONNECTIONFAILED:
                        break;
                    case ReturnType.LOGIN_SUCCESS:
//                        init();
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
                            tabsContainer.setVisibility(View.VISIBLE);
                            if (tabAdapter != null)
                                ((FaqFragment) tabAdapter.getItem(1)).init();
                        }
                        break;
                    case ReturnType.savedLead:
                        if (tabAdapter != null)
                            ((SupportFragment) tabAdapter.getItem(0)).setLeadAgain();
                        break;
                    case ReturnType.GetSupporters:
                        if (supporterView != null && supporterView.getAdapter() != null)
                            supporterView.getAdapter().notifyDataSetChanged();
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
        Intent a = new Intent(ConversationListActivity.this, ErxesActivity.class);
        startActivity(a);
        this.finish();
    }

    public void sendLead() {
        erxesRequest.sendLead();
    }

    private void initConversationChanged() {
        for (int i = 0; i < config.conversations.size(); i++) {
            if (disposabledChanged.size() > 0) {
                boolean have = false;
                for (int j = 0; j < disposabledChanged.size(); j++) {
                    if (disposabledChanged.get(j).equals(config.conversations.get(i)._id)) {
                        have = true;
                        break;
                    }
                }
                if (!have) {
                    disposabledChanged.add(config.conversations.get(i)._id);
                    clientChangedListen(config.conversations.get(i)._id);
                }
            } else {
                disposabledChanged.add(config.conversations.get(i)._id);
                clientChangedListen(config.conversations.get(i)._id);
            }
        }
    }

    public void clientChangedListen(final String conversationId) {
        if (runThreadInserted(conversationId))
            return;
        if (erxesRequest.apolloClient == null)
            return;
        if (conversationId != null) {
                opensourceChangedCall = erxesRequest.apolloClient
                        .subscribe(ConversationChangedSubscription.builder()
                                ._id(conversationId)
                                .build());
                initChangedConversation();
        }
    }

    private void initChangedConversation() {
        disposablesChanged.add(Rx2Apollo.from(opensourceChangedCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<ConversationChangedSubscription.Data>>() {
                            @Override
                            public void onNext(Response<ConversationChangedSubscription.Data> dataResponse) {
                                runOnUiThread(() -> {
                                    if (!dataResponse.hasErrors()) {
                                        if (dataResponse.data() != null &&
                                                dataResponse.data().conversationChanged().type().equalsIgnoreCase("closed")) {
                                            config.Logout(ConversationListActivity.this);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.e(TAG, "onerrorChanged ");
                                t.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                                Log.e(TAG, "oncompleteChanged");
                            }
                        }
                )
        );
    }

    private boolean runThreadInserted(final String conversationId) {
        if (!config.isNetworkConnected()) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                clientChangedListen(conversationId);
            }).start();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        config.setActivityConfig(this);
        erxesRequest = ErxesRequest.getInstance(config);
        erxesRequest.add(this);
        if (dataManager.getDataS(DataManager.customerId) == null) {
            startActivity();
            return;
        }
        if (TextUtils.isEmpty(dataManager.getDataS(DataManager.email)) &&
                TextUtils.isEmpty(dataManager.getDataS(DataManager.phone))) {
            dataManager.setData(DataManager.customerId, null);
            startActivity();
            return;
        }
        if (config.messengerdata.isShowChat()) {
            erxesRequest.getConversations();
        }
        erxesRequest.getLead();
        erxesRequest.getGEO();
        config.conversationId = null;

        dataManager.setData("chat_is_going", true);

        info_header.setBackgroundColor(config.colorCode);

        chat_is_going = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chat_is_going = false;
        dataManager.setData("chat_is_going", false);
        stopService(intent);
        disposabledChanged.clear();
        disposablesChanged.dispose();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        erxesRequest = ErxesRequest.getInstance(config);
        config = Config.getInstance(this);
        setContentView(R.layout.activity_conversation);

        viewpager = findViewById(R.id.viewpager);
        info_header = findViewById(R.id.info_header);
        container = findViewById(R.id.container);
        welcometext = findViewById(R.id.greetingMessage);
        title = findViewById(R.id.greetingTitle);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);
        fb = findViewById(R.id.fb);
        tw = findViewById(R.id.tw);
        yt = findViewById(R.id.yt);
        tabLayout = findViewById(R.id.tabs);
        tabsContainer = findViewById(R.id.tabsContainer);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        cancelImageView.setOnClickListener(v -> finish());
        initIcon();

        dataManager = DataManager.getInstance(this);

        intent = new Intent(ConversationListActivity.this, ListenerService.class);

        viewpager.setPagingEnabled(false);
        tabAdapter = new TabAdapter(getSupportFragmentManager(), this);


        viewpager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);
        tabLayout.setTabTextColors(getResources().getColor(R.color.md_grey_500), config.colorCode);

        if (config.knowledgeBaseTopic != null && config.knowledgeBaseTopic.categories != null) {
            tabsContainer.setVisibility(View.VISIBLE);
        }

        init();

        supporterView.setAdapter(new SupportAdapter(this, config.supporters));
        LinearLayoutManager supManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        Helper.display_configure(this, container, "#66000000");


        erxesRequest.getSupporters();

        fb.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        tw.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        yt.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
        this.findViewById(R.id.fbcontainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (config.messengerdata.getFacebook() != null && config.messengerdata.getFacebook().contains("http")) {
                    startFacebook(config.messengerdata.getFacebook());
                }
            }
        });
        this.findViewById(R.id.twcontainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (config.messengerdata.getTwitter() != null && config.messengerdata.getTwitter().contains("http")) {
                    startTwitter(config.messengerdata.getTwitter());
                }
            }
        });
        this.findViewById(R.id.ytcontainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (config.messengerdata.getYoutube() != null && config.messengerdata.getYoutube().contains("http")) {
                    startYoutube(config.messengerdata.getYoutube());
                }
            }
        });
    }

    private void initIcon() {
        Glide.with(this).load(config.getCancelIcon(this, config.getInColor(config.colorCode))).into(cancelImageView);
    }

    private void init() {
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
