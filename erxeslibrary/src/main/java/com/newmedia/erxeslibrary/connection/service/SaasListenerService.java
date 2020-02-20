package com.newmedia.erxeslibrary.connection.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.erxes.io.saas.SaasConversationMessageInsertedSubscription;
import com.erxes.io.saas.type.CustomType;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.connection.helper.JsonCustomTypeAdapter;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.OkHttpClient;

public class SaasListenerService extends Service {

    private static final String TAG = SaasListenerService.class.getName();
    private ApolloClient apolloClient;
    private ErxesRequest erxesRequest;
    private CompositeDisposable disposables = new CompositeDisposable();
    Config config;

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
        DataManager dataManager;
        dataManager = DataManager.getInstance(this);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        apolloClient = ApolloClient.builder()
                .serverUrl(dataManager.getDataS("host3100"))
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(dataManager.getDataS("host3300"), okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                .build();


        Log.e(TAG, "oncreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "destory");
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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
            if (disposables.size() != config.conversations.size()) {
                disposables.clear();
                for (int i = 0; i < config.conversations.size(); i++) {
                    conversation_listen(config.conversations.get(i).id);
                }
            }
        } else {
            conversation_listen(id);
        }

        return super.onStartCommand(intent, flags, startId);


    }

    private boolean run_thread(final String conversationId) {
        if (!isNetworkConnected()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Log.e(TAG, "subscribe thread running ");
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    conversation_listen(conversationId);
                }
            }).start();
            return true;
        }
        return false;
    }

    public void conversation_listen(final String conversationId) {
        if (run_thread(conversationId))
            return;
        ApolloSubscriptionCall<SaasConversationMessageInsertedSubscription.Data> subscriptionCall;
        if (apolloClient == null)
            return;
        subscriptionCall = apolloClient
                .subscribe(SaasConversationMessageInsertedSubscription.builder()
                        .id(conversationId)
                        .build());
        disposables.add(Rx2Apollo.from(subscriptionCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<SaasConversationMessageInsertedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                                Log.e(TAG, "onStartedSaas " + conversationId);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onErrorSaas " + conversationId);
                                e.printStackTrace();
                                run_thread(conversationId);
                            }

                            @Override
                            public void onNext(Response<SaasConversationMessageInsertedSubscription.Data> response) {
                                if (!response.hasErrors()) {
                                    if (response.data().conversationMessageInserted() != null) {
                                        DataManager dataManager = DataManager.getInstance(SaasListenerService.this);
                                        if (dataManager.getDataB("chatIsGoing")) {
                                            ConversationMessage conversationMessage = ConversationMessage.convertSaas(response.data().conversationMessageInserted());
                                            if (config.conversationMessages.size() > 0) {
                                                if (!config.conversationMessages.get(config.conversationMessages.size() - 1).id
                                                        .equals(conversationMessage.id) && !conversationMessage.internal) {
                                                    config.conversationMessages.add(conversationMessage);
                                                }
                                            }
                                            for (int i = 0; i < config.conversations.size(); i++) {
                                                if (config.conversations.get(i).id.equals(conversationId)) {
                                                    if (!config.conversations.get(i).conversationMessages
                                                            .get(config.conversations.get(i).conversationMessages.size() - 1).id
                                                            .equals(conversationMessage.id) && !conversationMessage.internal)
                                                        config.conversations.get(i).conversationMessages.add(conversationMessage);
                                                    break;
                                                }
                                            }
                                            erxesRequest.notefyAll(ReturntypeUtil.COMINGNEWMESSAGE, null, null);

                                            for (int i = 0; i < config.conversations.size(); i++) {
                                                if (config.conversations.get(i).id.equals(response.data().conversationMessageInserted().conversationId())) {
                                                    config.conversations.get(i).content = response.data().conversationMessageInserted().content();
                                                    config.conversations.get(i).isread = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onComplete() {
                                Log.e(TAG, "onCompleteSaas");
                            }
                        }
                )
        );
    }

}
