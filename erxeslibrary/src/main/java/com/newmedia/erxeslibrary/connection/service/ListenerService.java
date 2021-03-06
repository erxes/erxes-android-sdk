package com.newmedia.erxeslibrary.connection.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.erxes.io.opens.ConversationMessageInsertedSubscription;
import com.erxes.io.opens.type.CustomType;
import com.erxes.io.saas.SaasConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.connection.helper.JsonCustomTypeAdapter;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.DisposableSubscriber;
import okhttp3.OkHttpClient;

public class ListenerService extends Service {

    private static final String TAG = ListenerService.class.getName();
    private ApolloClient apolloClient;
    private ErxesRequest erxesRequest;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private Config config;
    private DataManager dataManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);

        dataManager = DataManager.getInstance(this);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        apolloClient = ApolloClient.builder()
                .serverUrl(dataManager.getDataS("host3100"))
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(dataManager.getDataS("host3300"), okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String id = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                id = bundle.getString("id", null);
        }
        if (id == null) {
            if (disposables.size() != config.conversationIds.size()) {
                disposables.clear();
                for (int i = 0; i < config.conversationIds.size(); i++) {
                    conversationListen(config.conversationIds.get(i));
                }
            }
        } else {
            conversationListen(id);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean run_thread(final String conversationId) {
        if (!config.isNetworkConnected()) {
            new Thread(() -> {
                try {
                    Log.e(TAG, "subscribe thread running ");
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                conversationListen(conversationId);
            }).start();
            return true;
        }
        return false;
    }

    public void conversationListen(final String conversationId) {
        if (run_thread(conversationId))
            return;
        if (!dataManager.getDataS("host3300").contains("app.erxes.io")) {
            listenOpensource(conversationId);
        } else {
            listenSaas(conversationId);
        }

    }

    private void listenOpensource (final String conversationId) {
        ApolloSubscriptionCall<ConversationMessageInsertedSubscription.Data> subscriptionCall;
        if (apolloClient == null)
            return;
        subscriptionCall = apolloClient
                .subscribe(ConversationMessageInsertedSubscription.builder()
                        .id(conversationId)
                        .build());
        disposables.add(Rx3Apollo.from(subscriptionCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<ConversationMessageInsertedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                run_thread(conversationId);
                            }

                            @Override
                            public void onNext(Response<ConversationMessageInsertedSubscription.Data> response) {
                                if (!response.hasErrors()) {
                                    if (response.getData().conversationMessageInserted() != null) {
                                        DataManager dataManager = DataManager.getInstance(ListenerService.this);
                                        if (dataManager.getDataB("chatIsGoing")) {
                                            ConversationMessage conversationMessage = ConversationMessage.convert(response.getData().conversationMessageInserted());
                                            erxesRequest.notefyAll(ReturntypeUtil.COMINGNEWMESSAGE, null, null, conversationMessage);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onComplete() {
                            }
                        }
                )
        );
    }

    private void listenSaas (final String conversationId) {
        ApolloSubscriptionCall<SaasConversationMessageInsertedSubscription.Data> subscriptionCall;
        if (apolloClient == null)
            return;
        subscriptionCall = apolloClient
                .subscribe(SaasConversationMessageInsertedSubscription.builder()
                        .id(conversationId)
                        .build());
        disposables.add(Rx3Apollo.from(subscriptionCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<SaasConversationMessageInsertedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                run_thread(conversationId);
                            }

                            @Override
                            public void onNext(Response<SaasConversationMessageInsertedSubscription.Data> response) {
                                if (!response.hasErrors()) {
                                    if (response.getData().conversationMessageInserted() != null) {
                                        DataManager dataManager = DataManager.getInstance(ListenerService.this);
                                        if (dataManager.getDataB("chatIsGoing")) {
                                            ConversationMessage conversationMessage = ConversationMessage.convertSaas(response.getData().conversationMessageInserted());
                                            erxesRequest.notefyAll(ReturntypeUtil.COMINGNEWMESSAGE, null, null, conversationMessage);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onComplete() {
                            }
                        }
                )
        );
    }
}
