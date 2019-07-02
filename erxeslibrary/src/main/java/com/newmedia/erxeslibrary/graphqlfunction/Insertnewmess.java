package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.DB;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import java.util.List;

import javax.annotation.Nonnull;

public class Insertnewmess {
    final static String TAG = "insertnew";
    private ErxesRequest ER;
    private Config config ;
    private Activity context;
    private String message;
    public Insertnewmess(ErxesRequest ER, Activity context) {
        this.ER = ER;
        this.context = context;
        config = Config.getInstance(context);
    }
    public void run( String message, List<AttachmentInput> list){
        this.message = message;
        ER.apolloClient.mutate(InsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(message)
                .conversationId("")
                .attachments(list)
                .build())
                .enqueue(request);
    }
    private ApolloCall.Callback<InsertMessageMutation.Data> request = new ApolloCall.Callback<InsertMessageMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {
            if(response.hasErrors()) {
                Log.d(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR,null,response.errors().get(0).message());
            }
            else {
                Log.d(TAG, "cid " + response.data().insertMessage().conversationId());

                Conversation conversation = Conversation.update(response.data().insertMessage(),message,config);
                ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),message,config);
                DB.save(conversation);
                DB.save(a);
                Intent intent2 = new Intent(context, ListenerService.class);
                intent2.putExtra("id",config.conversationId);
                context.startService(intent2);
//                    ListenerService.conversation_listen(config.conversationId);

                ER.notefyAll(ReturnType.Mutation_new,response.data().insertMessage().conversationId(),null);


            }
        }
        @Override
        public void onFailure(@Nonnull ApolloException e) {
            e.printStackTrace();
            ER.notefyAll( ReturnType.CONNECTIONFAILED,null,e.getMessage());
            Log.d(TAG, "failed ");
        }
    };
}
