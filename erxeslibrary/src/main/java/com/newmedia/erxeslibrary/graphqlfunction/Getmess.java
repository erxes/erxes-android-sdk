package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Getmess {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private String conversationid;
    private Config config;

    public Getmess(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        this.conversationid = conversationid;
        ER.apolloClient.query(MessagesQuery.builder()
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

                ER.notefyAll(ReturnType.Getmessages, conversationid, null);
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, conversationid, null);
            Log.d(TAG, "Getmessages failed ");
        }
    };
}
