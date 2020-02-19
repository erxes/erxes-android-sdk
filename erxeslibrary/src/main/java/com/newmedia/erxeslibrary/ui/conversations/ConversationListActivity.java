package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;

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
import com.erxes.io.opens.ConversationChangedSubscription;
import com.erxes.io.saas.SaasConversationChangedSubscription;
import com.newmedia.erxeslibrary.connection.service.SaasListenerService;
import com.newmedia.erxeslibrary.helper.CustomViewPager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.connection.service.ListenerService;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.ui.conversations.adapter.SupportAdapter;
import com.newmedia.erxeslibrary.ui.conversations.adapter.TabAdapter;
import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.ErxesActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class ConversationListActivity extends AppCompatActivity implements ErxesObserver {

    public static boolean chatIsGoing = false;

    private RecyclerView supporterView;
    private TextView welcometext, date, title;
    private ViewGroup infoHeader, container;
    private Config config;
    private ErxesRequest erxesRequest;
    private DataManager dataManager;
    private TabAdapter tabAdapter;
    private TabLayout tabLayout;
    private ImageView cancelImageView;
    private LinearLayout tabsContainer;
    private Intent intent;
    private List<String> disposabledChanged = new ArrayList<>();
    private CompositeDisposable disposablesChanged = new CompositeDisposable();
    private ApolloSubscriptionCall<ConversationChangedSubscription.Data> opensourceChangedCall;
    private ApolloSubscriptionCall<SaasConversationChangedSubscription.Data> saasChangedCall;

    @Override
    public void notify(final int returnType, final String conversationId, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturntypeUtil.COMINGNEWMESSAGE:
                        if (tabAdapter != null && ((SupportFragment) tabAdapter.getItem(0))
                                .recyclerView != null) {
                            ((SupportFragment) tabAdapter.getItem(0))
                                    .recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        break;
                    case ReturntypeUtil.GETCONVERSATION:
                        if (tabAdapter != null && ((SupportFragment) tabAdapter.getItem(0))
                                .recyclerView != null) {
                            ((SupportFragment) tabAdapter.getItem(0))
                                    .recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        if (config.conversations.size() > 0) {
                            initParentConversationChanged();
                        }
                        break;
                    case ReturntypeUtil.SERVERERROR:
                        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
                        break;
//                    case ReturntypeUtil.LEAD:
//                        if (tabAdapter != null)
//                            ((SupportFragment) tabAdapter.getItem(0)).setLead();
//                        break;
                    case ReturntypeUtil.FAQ:
                        if (tabLayout != null) {
                            tabsContainer.setVisibility(View.VISIBLE);
                            if (tabAdapter != null)
                                ((FaqFragment) tabAdapter.getItem(1)).init();
                        }
                        break;
                    case ReturntypeUtil.SAVEDLEAD:
                        if (tabAdapter != null)
                            ((SupportFragment) tabAdapter.getItem(0)).setLeadAgain();
                        break;
                    case ReturntypeUtil.GETSUPPORTERS:
                        if (supporterView != null && supporterView.getAdapter() != null)
                            supporterView.getAdapter().notifyDataSetChanged();
                        break;
//                    case ReturntypeUtil.MUTATIONNEW:
//                        if (tabAdapter != null && ((SupportFragment) tabAdapter.getItem(0))
//                                .recyclerView != null) {
//                            ((SupportFragment) tabAdapter.getItem(0))
//                                    .recyclerView.getAdapter().notifyDataSetChanged();
//                        }
//                        if (config.conversations.size() > 0) {
//                            initParentConversationChanged();
//                        }
//                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        erxesRequest = ErxesRequest.getInstance(config);
        config = Config.getInstance(this);
        ErxesHelper.changeLanguage(this, config.language);

        setContentView(R.layout.activity_conversation);

        CustomViewPager viewpager = findViewById(R.id.viewpager);
        infoHeader = findViewById(R.id.info_header);
        container = findViewById(R.id.container);
        welcometext = findViewById(R.id.greetingMessage);
        title = findViewById(R.id.greetingTitle);
        date = findViewById(R.id.date);
        supporterView = findViewById(R.id.supporters);
        ImageView fb = findViewById(R.id.fb);
        ImageView tw = findViewById(R.id.tw);
        ImageView yt = findViewById(R.id.yt);
        tabLayout = findViewById(R.id.tabs);
        tabsContainer = findViewById(R.id.tabsContainer);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initIcon();

        dataManager = DataManager.getInstance(this);

        if (!dataManager.getDataS("host3300").contains("app.erxes.io")) {
            intent = new Intent(ConversationListActivity.this, ListenerService.class);
        } else {
            intent = new Intent(ConversationListActivity.this, SaasListenerService.class);
        }

        viewpager.setPagingEnabled(false);
        tabAdapter = new TabAdapter(getSupportFragmentManager(), this);

        viewpager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewpager);
        tabLayout.setSelectedTabIndicatorColor(config.colorCode);
        tabLayout.setTabRippleColorResource(R.color.md_deep_purple_300);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabTextColors(getResources().getColor(R.color.md_grey_500),
                getResources().getColor(R.color.md_black_1000));

        erxesRequest.getSupporters();
//        erxesRequest.getLead();
        erxesRequest.getFAQ();
        init();

        if (config.knowledgeBaseTopic != null && config.knowledgeBaseTopic.categories != null) {
            tabsContainer.setVisibility(View.VISIBLE);
        }

        supporterView.setAdapter(new SupportAdapter(this, config.supporters));
        LinearLayoutManager supManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        supporterView.setLayoutManager(supManager);
        ErxesHelper.display_configure(this, container, "#66000000");


        fb.getDrawable().setColorFilter(config.getInColorGray(config.colorCode), PorterDuff.Mode.SRC_ATOP);
        tw.getDrawable().setColorFilter(config.getInColorGray(config.colorCode), PorterDuff.Mode.SRC_ATOP);
        yt.getDrawable().setColorFilter(config.getInColorGray(config.colorCode), PorterDuff.Mode.SRC_ATOP);
//        yt.getDrawable().setColorFilter(Color.parseColor("#dad8d8"), PorterDuff.Mode.SRC_ATOP);
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
        Glide.with(this).load(config.getCancelIcon(config.getInColor(config.colorCode))).into(cancelImageView);
    }

    private void init() {
        date.setText(config.now());
        date.setTextColor(config.getInColorGray(config.colorCode));
        title.setTextColor(config.getInColor(config.colorCode));
        welcometext.setTextColor(config.getInColorGray(config.colorCode));
        if (config.messengerdata.getMessages() != null && config.messengerdata.getMessages().getGreetings() != null) {
            if (!TextUtils.isEmpty(config.messengerdata.getMessages().getGreetings().getTitle()))
                title.setText(config.messengerdata.getMessages().getGreetings().getTitle());
            if (!TextUtils.isEmpty(config.messengerdata.getMessages().getGreetings().getMessage()))
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

    @Override
    protected void onPause() {
        super.onPause();
        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config = Config.getInstance(this);
        config.setActivityConfig(this);
        erxesRequest = ErxesRequest.getInstance(config);
        erxesRequest.add(this);
        if (dataManager.getDataS(DataManager.CUSTOMERID) == null) {
            startActivity();
            return;
        }
        if (TextUtils.isEmpty(dataManager.getDataS(DataManager.EMAIL)) &&
                TextUtils.isEmpty(dataManager.getDataS(DataManager.PHONE))) {
            dataManager.setData(DataManager.CUSTOMERID, null);
            startActivity();
            return;
        }
        if (config.messengerdata.isShowChat()) {
            erxesRequest.getConversations();
        }

//        if (config.formConnect != null && tabAdapter != null) {
//            ((SupportFragment) tabAdapter.getItem(0)).setLead();
//        }
        if (config.supporters != null && config.supporters.size() > 0) {
            if (supporterView != null && supporterView.getAdapter() != null)
                supporterView.getAdapter().notifyDataSetChanged();
        }
        if (config.knowledgeBaseTopic != null && config.knowledgeBaseTopic.categories != null) {
            if (tabLayout != null && tabsContainer.getVisibility() == View.GONE) {
                tabsContainer.setVisibility(View.VISIBLE);
                if (tabAdapter != null)
                    ((FaqFragment) tabAdapter.getItem(1)).init();
            }
        }

        config.conversationId = null;

        dataManager.setData("chatIsGoing", true);

        infoHeader.setBackgroundColor(config.colorCode);

        chatIsGoing = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatIsGoing = false;
        dataManager.setData("chatIsGoing", false);
        stopService(intent);
        disposabledChanged.clear();
        disposablesChanged.dispose();
    }

    private void startActivity() {
        Intent a = new Intent(ConversationListActivity.this, ErxesActivity.class);
        startActivity(a);
        this.finish();
    }

    public void sendLead() {
        erxesRequest.sendLead();
    }

    private void initParentConversationChanged() {
        stopService(config.intent);
        startService(config.intent);
        initConversationChanged();
    }

    private void initConversationChanged() {
        for (int i = 0; i < config.conversations.size(); i++) {
            if (disposabledChanged.size() > 0) {
                boolean have = false;
                for (int j = 0; j < disposabledChanged.size(); j++) {
                    if (disposabledChanged.get(j).equals(config.conversations.get(i).id)) {
                        have = true;
                        break;
                    }
                }
                if (!have) {
                    disposabledChanged.add(config.conversations.get(i).id);
                    clientChangedListen(config.conversations.get(i).id);
                }
            } else {
                disposabledChanged.add(config.conversations.get(i).id);
                clientChangedListen(config.conversations.get(i).id);
            }
        }
    }

    public void clientChangedListen(final String conversationId) {
        if (runThreadInserted(conversationId))
            return;
        if (erxesRequest.apolloClient == null)
            return;
        if (conversationId != null) {
            if (!dataManager.getDataS("host3300").contains("app.erxes.io")) {
                opensourceChangedCall = erxesRequest.apolloClient
                        .subscribe(ConversationChangedSubscription.builder()
                                .id(conversationId)
                                .build());
                initChangedConversation();
            } else {
                saasChangedCall = erxesRequest.apolloClient
                        .subscribe(SaasConversationChangedSubscription.builder()
                                .id(conversationId)
                                .build());
                initChangedConversationSaas();
            }
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
                                if (!dataResponse.hasErrors()) {
                                    if (dataResponse.data() != null && dataResponse.data().conversationChanged().type().equalsIgnoreCase("closed")) {
                                        if (config.messengerdata.isForceLogoutWhenResolve()) {
                                                config.Logout(ConversationListActivity.this);
                                        } else {
                                            for (int i = 0 ; i < config.conversations.size(); i ++) {
                                                if (config.conversations.get(i).id.equals(dataResponse.data().conversationChanged().conversationId())) {
                                                    config.conversations.get(i).status = dataResponse.data().conversationChanged().type();
                                                    config.conversations.remove(i);
                                                    erxesRequest.notefyAll(ReturntypeUtil.GETCONVERSATION, null, null);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.e(getClass().getName(), "onerrorChanged ");
                                t.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                                Log.e(getClass().getName(), "oncompleteChanged");
                            }
                        }
                )
        );
    }

    private void initChangedConversationSaas() {
        disposablesChanged.add(Rx2Apollo.from(saasChangedCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<SaasConversationChangedSubscription.Data>>() {
                            @Override
                            public void onNext(Response<SaasConversationChangedSubscription.Data> dataResponse) {
                                if (!dataResponse.hasErrors()) {
                                    if (dataResponse.data() != null && dataResponse.data().conversationChanged().type().equals("closed")) {
                                        if (config.messengerdata.isForceLogoutWhenResolve()) {
                                                config.Logout(ConversationListActivity.this);
                                        } else {
                                            for (int i = 0 ; i < config.conversations.size(); i ++) {
                                                if (config.conversations.get(i).id.equals(dataResponse.data().conversationChanged().conversationId())) {
                                                    config.conversations.get(i).status = dataResponse.data().conversationChanged().type();
                                                    config.conversations.remove(i);
                                                    erxesRequest.notefyAll(ReturntypeUtil.GETCONVERSATION, null, null);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.e(getClass().getName(), "onerrorChanged ");
                                t.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                                Log.e(getClass().getName(), "oncompleteChanged");
                            }
                        }
                )
        );
    }

    private boolean runThreadInserted(final String conversationId) {
        if (!config.isNetworkConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    clientChangedListen(conversationId);
                }
            }).start();
            return true;
        }
        return false;
    }


    private void startYoutube(String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        startActivity(intent);
    }

    private void startFacebook(String s) {
        String mUri = s;
        try {
            Uri uri;

            int versionCode = getPackageManager()
                    .getPackageInfo("com.facebook.katana", 0)
                    .versionCode;

            if (versionCode >= 3002850) {
                mUri = mUri.toLowerCase().replace("www.", "m.");
                if (!mUri.startsWith("https")) {
                    mUri = "https://" + mUri;
                }
                uri = Uri.parse("fb://facewebmodal/f?href=" + mUri);
            } else {
                String pageID = mUri.substring(mUri.lastIndexOf("/"));

                uri = Uri.parse("fb://page" + pageID);
            }
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Throwable e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mUri)));
        }
    }

    private void startTwitter(String s) {
        String mUri = s;
        if (mUri.startsWith("https://twitter.com/")) {
            mUri = mUri.replace("https://twitter.com/", "");
        }
        String uName = mUri;
        try {
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + uName)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + uName)));
        }
    }
}
