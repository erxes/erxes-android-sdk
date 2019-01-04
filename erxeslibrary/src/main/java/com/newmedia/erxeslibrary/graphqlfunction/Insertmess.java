package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.DB;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.json.JSONObject;

import java.util.List;

import javax.annotation.Nonnull;

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
        public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {

            if(response.hasErrors()) {
                Log.d(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR,conversationId,response.errors().get(0).message());
            }
            else {
                ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),message,config);
                DB.save(a);
                ER.notefyAll(ReturnType.Mutation,conversationId,null);
            }
        }
        @Override
        public void onFailure(@Nonnull ApolloException e) {
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
            Log.d(TAG, "failed ");
        }
    };
}
