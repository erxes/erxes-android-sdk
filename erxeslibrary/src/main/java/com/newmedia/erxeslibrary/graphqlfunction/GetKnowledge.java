package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetKnowledge {
    final static String TAG = "GetKnowledge";
    private ErxesRequest ER;
    private Config config;
    private Activity activity;

    public GetKnowledge(ErxesRequest ER, Activity activity) {
        this.ER = ER;
        config = Config.getInstance(activity);
        this.activity = activity;
    }

    public void run() {
        Log.e(TAG, "run: " + config.messengerdata.getKnowledgeBaseTopicId());
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null) {
//            ER.apolloClient.query(FaqGetQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
//                    .enqueue(request);
            Rx2Apollo.from(ER.apolloClient
                    .query(FaqGetQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
                    .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(a);
        }

    }
    private Observer a = new Observer<Response<FaqGetQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<FaqGetQuery.Data> response) {
            if (!response.hasErrors()) {
                config.knowledgeBaseTopic = KnowledgeBaseTopic.convert(response.data());
                ER.notefyAll(ReturnType.FAQ, null, null);
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
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
    private ApolloCall.Callback<FaqGetQuery.Data> request = new ApolloCall.Callback<FaqGetQuery.Data>() {
        @Override
        public void onResponse(@NotNull final Response<FaqGetQuery.Data> response) {
            if (!response.hasErrors()) {
                config.knowledgeBaseTopic = KnowledgeBaseTopic.convert(response.data());
                ER.notefyAll(ReturnType.FAQ, null, null);
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.e(TAG, "failed ");
            e.printStackTrace();

        }
    };
}
