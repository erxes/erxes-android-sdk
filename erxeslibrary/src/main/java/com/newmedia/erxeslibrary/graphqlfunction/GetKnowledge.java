package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import javax.annotation.Nonnull;

import io.realm.Realm;

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
        ER.apolloClient.query(FaqGetQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
                .enqueue(request);
    }

    private ApolloCall.Callback<FaqGetQuery.Data> request = new ApolloCall.Callback<FaqGetQuery.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<FaqGetQuery.Data> response) {
            if (!response.hasErrors()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Realm realm = DB.getDB();
                        realm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realm.delete(KnowledgeBaseTopic.class);
                                realm.delete(KnowledgeBaseCategory.class);
                                realm.delete(KnowledgeBaseArticle.class);
                                KnowledgeBaseTopic a = new KnowledgeBaseTopic();
                                a.convert(response.data());
                                realm.insertOrUpdate(a);
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                ER.notefyAll(ReturnType.FAQ, null, null);
                            }
                        });
                    }
                });
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.e(TAG, "failed ");
            e.printStackTrace();

        }
    };
}
