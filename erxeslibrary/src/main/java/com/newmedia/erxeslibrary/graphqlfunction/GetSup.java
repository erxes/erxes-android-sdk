package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.ConversationDetailQuery;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.model.User;

import javax.annotation.Nonnull;

import io.realm.Realm;

public class GetSup {
    final static String TAG = "GETSUP";
    private ErxesRequest ER;
    private Config config ;
    public GetSup(ErxesRequest ER, Context context) {
        this.ER = ER;
        config = Config.getInstance(context);

    }
    public void run(){
        ER.apolloClient.query(ConversationDetailQuery.builder().integ(config.integrationId).build())
                .enqueue(request);
    }

    private ApolloCall.Callback<ConversationDetailQuery.Data> request =  new ApolloCall.Callback<ConversationDetailQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ConversationDetailQuery.Data> response) {
            if(!response.hasErrors()) {
                config.isMessengerOnline = response.data().conversationDetail().isOnline();
                Realm inner = Realm.getInstance(Helper.getRealmConfig());
                inner.beginTransaction();
                inner.copyToRealmOrUpdate(User.convert(response.data().conversationDetail().supporters()));
                inner.commitTransaction();
                inner.close();
                ER.notefyAll(ReturnType.GetSupporters,null ,null);
            }
            else{
                Log.d(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR,null,response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null, e.getMessage());
            Log.d(TAG, "failed ");
            e.printStackTrace();

        }
    };
}
