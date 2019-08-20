package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.model.Conversation;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;

public class Getconv {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config ;
    private DataManager dataManager;
    private Context context;
    public Getconv(ErxesRequest ER, Activity context) {
        this.ER = ER;
        this.context = context;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);

    }
    public void run(){
//        ER.apolloClient.query(ConversationsQuery.builder()
//                        .integrationId(config.integrationId)
//                        .customerId(config.customerId).build())
//                .enqueue(request);
        Rx2Apollo.from(ER.apolloClient
                .query(ConversationsQuery.builder()
                        .integrationId(config.integrationId)
                        .customerId(config.customerId).build())
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a);
    }
    private Observer a = new Observer<Response<ConversationsQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<ConversationsQuery.Data> response) {
            if(response.data().conversations().size()>0) {
                if (config.conversations != null && config.conversations.size() > 0)
                    config.conversations.clear();
                if (config.conversations != null) {
                    for (Conversation conversation : Conversation.convert(response,config)) {
                        if (conversation.status.equalsIgnoreCase("open"))
                            config.conversations.add(conversation);
                    }
                }

                Log.d(TAG,"Getconversation ok ");
            }
            else
                Log.d(TAG,"Getconversation 0 ");
            ER.notefyAll(ReturnType.Getconversation,null,null);
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };
    private ApolloCall.Callback<ConversationsQuery.Data> request = new ApolloCall.Callback<ConversationsQuery.Data>() {
        @Override
        public void onResponse(@NotNull Response<ConversationsQuery.Data> response) {

            if(response.data().conversations().size()>0) {
                if (config.conversations != null && config.conversations.size() > 0)
                    config.conversations.clear();
                if (config.conversations != null) {
                    for (Conversation conversation : Conversation.convert(response,config)) {
                        if (conversation.status.equalsIgnoreCase("open"))
                        config.conversations.add(conversation);
                    }
                }

                Log.d(TAG,"Getconversation ok ");
            }
            else
                Log.d(TAG,"Getconversation 0 ");
            ER.notefyAll(ReturnType.Getconversation,null,null);
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            Log.d(TAG,"Getconversation failed ");
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
        }
    };
}
