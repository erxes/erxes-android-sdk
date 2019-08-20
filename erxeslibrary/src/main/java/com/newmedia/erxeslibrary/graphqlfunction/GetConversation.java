package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.Conversation;

import org.jetbrains.annotations.NotNull;

public class GetConversation {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private Config config ;
    public GetConversation(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }
    public void run(){
        erxesRequest.apolloClient.query(ConversationsQuery.builder()
                        .integrationId(config.integrationId)
                        .customerId(config.customerId).build())
                .enqueue(request);
    }
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
            }
            erxesRequest.notefyAll(Returntype.GETCONVERSATION,null,null);
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED,null,e.getMessage());
        }
    };
}
