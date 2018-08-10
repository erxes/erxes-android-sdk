package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.Model.Conversation;

import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;

public class Getconv {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config ;
    private DataManager dataManager;
    private Context context;
    public Getconv(ErxesRequest ER, Context context) {
        this.ER = ER;
        this.context = context;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);

    }
    public void run(){
        ER.apolloClient.query(ConversationsQuery.builder().integrationId(config.integrationId).
                customerId(config.customerId).build()).enqueue(request);
    }
    private ApolloCall.Callback<ConversationsQuery.Data> request = new ApolloCall.Callback<ConversationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ConversationsQuery.Data> response) {

            if(response.data().conversations().size()>0) {
                final List<Conversation> data = Conversation.convert(response,config);

                Realm inner = Realm.getDefaultInstance();
                inner.beginTransaction();
                inner.delete(Conversation.class);
                inner.copyToRealm(data);
                inner.commitTransaction();
                inner.close();

                Log.d(TAG,"Getconversation ok ");
            }
            else
                Log.d(TAG,"Getconversation 0 ");
            ER.notefyAll(ReturnType.Getconversation,null,null);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.d(TAG,"Getconversation failed ");
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
        }
    };
}
