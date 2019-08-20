package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import org.jetbrains.annotations.NotNull;

public class GetKnowledge {
    final static String TAG = "GetKnowledge";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetKnowledge(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null)
            erxesRequest.apolloClient.query(FaqGetQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
                    .enqueue(request);
    }

    private ApolloCall.Callback<FaqGetQuery.Data> request = new ApolloCall.Callback<FaqGetQuery.Data>() {
        @Override
        public void onResponse(@NotNull final Response<FaqGetQuery.Data> response) {
            if (!response.hasErrors()) {
                config.knowledgeBaseTopic = KnowledgeBaseTopic.convert(response.data());
                erxesRequest.notefyAll(Returntype.FAQ, null, null);
            } else {
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED, null, e.getMessage());
            e.printStackTrace();

        }
    };
}
