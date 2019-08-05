package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import org.jetbrains.annotations.NotNull;

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
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null)
            ER.apolloClient.query(FaqGetQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
                    .enqueue(request);
    }

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
