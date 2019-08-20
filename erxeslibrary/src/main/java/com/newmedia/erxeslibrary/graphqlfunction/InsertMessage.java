package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.text.TextUtils;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InsertMessage {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private Config config ;
    private String conversationId,mContent;
    public InsertMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }
    public void run( String mContent, final String conversationId,List<AttachmentInput> list){
        this.mContent = mContent;
        if (TextUtils.isEmpty(this.mContent)) {
            this.mContent = "This message has an attachment";
        }

        this.conversationId = conversationId;
        InsertMessageMutation.Builder temp =InsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(this.mContent)
                .attachments(list)
                .conversationId(conversationId);
        erxesRequest.apolloClient.mutate(temp.build()).enqueue(request);
    }

    private ApolloCall.Callback<InsertMessageMutation.Data> request = new ApolloCall.Callback<InsertMessageMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<InsertMessageMutation.Data> response) {

            if(response.hasErrors()) {
                erxesRequest.notefyAll(Returntype.SERVERERROR,conversationId,response.errors().get(0).message());
            } else {
                ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),mContent,config);
                config.conversationMessages.add(a);
                erxesRequest.notefyAll(Returntype.MUTATION,conversationId,null);
            }
        }
        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED,null,e.getMessage());
        }
    };
}
