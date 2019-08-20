package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GetMessage {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private String conversationid;
    private Config config;

    public GetMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        this.conversationid = conversationid;
        erxesRequest.apolloClient.query(MessagesQuery.builder()
                .conversationId(conversationid)
                .build()).enqueue(request);
    }

    private ApolloCall.Callback<MessagesQuery.Data> request = new ApolloCall.Callback<MessagesQuery.Data>() {
        @Override
        public void onResponse(@NotNull Response<MessagesQuery.Data> response) {

            if (response.data().messages().size() > 0) {
                if (config.conversationMessages.size() > 0)
                    config.conversationMessages.clear();
                List<ConversationMessage> conversationMessages = ConversationMessage.convert(response, conversationid);
                for (ConversationMessage message : conversationMessages) {
                    if (!message.internal)
                        config.conversationMessages.add(message);
                }

                erxesRequest.notefyAll(Returntype.GETMESSAGES, conversationid, null);
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED, conversationid, null);
        }
    };
}
