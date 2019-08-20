package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Insertmess {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config ;
    private String conversationId,message;
    public Insertmess(ErxesRequest ER, Context context) {
        this.ER = ER;
        config = Config.getInstance(context);
    }
    public void run( String message, final String conversationId,List<AttachmentInput> list){
        if (TextUtils.isEmpty(message)) {
            message = "This message has an attachment";
        }
        this.message = message;
        this.conversationId = conversationId;
        InsertMessageMutation.Builder temp =InsertMessageMutation.builder().
                integrationId(config.integrationId).
                customerId(config.customerId).
                message(message).
                attachments(list).
                conversationId(conversationId);
        ER.apolloClient.mutate(temp.build()).enqueue(request);
    }

    private ApolloCall.Callback<InsertMessageMutation.Data> request = new ApolloCall.Callback<InsertMessageMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<InsertMessageMutation.Data> response) {

            if(response.hasErrors()) {
                ER.notefyAll(ReturnType.SERVERERROR,conversationId,response.errors().get(0).message());
            } else {
                ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),message,config);
                config.conversationMessages.add(a);
                ER.notefyAll(ReturnType.Mutation,conversationId,null);
            }
        }
        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
        }
    };
}
