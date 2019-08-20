package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InsertNewMessage {
    final static String TAG = "insertnew";
    private ErxesRequest erxesRequest;
    private Config config;
    private Context context;
    private String mContent;

    public InsertNewMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        this.context = context;
        config = Config.getInstance(context);
    }

    public void run(String mContent, List<AttachmentInput> list) {
        if (TextUtils.isEmpty(mContent)) {
            mContent = "This message has an attachment";
        }
        this.mContent = mContent;
        erxesRequest.apolloClient.mutate(InsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(mContent)
                .conversationId("")
                .attachments(list)
                .build())
                .enqueue(request);
    }

    private ApolloCall.Callback<InsertMessageMutation.Data> request = new ApolloCall.Callback<InsertMessageMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<InsertMessageMutation.Data> response) {
            if (response.hasErrors()) {
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            } else {
                Conversation conversation = Conversation.update(response.data().insertMessage(), mContent, config);
                ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(), mContent, config);
                config.conversations.add(conversation);
                config.conversationMessages.add(a);
                Intent intent = new Intent(context, ListenerService.class);
                intent.putExtra("id", config.conversationId);
                context.startService(intent);
                erxesRequest.notefyAll(Returntype.MUTATIONNEW, response.data().insertMessage().conversationId(), null);
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED, null, e.getMessage());
        }
    };
}
