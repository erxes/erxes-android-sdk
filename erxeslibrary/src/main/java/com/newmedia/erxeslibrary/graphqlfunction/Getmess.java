package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.Model.ConversationMessage;

import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Getmess {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private String conversationid;
    public Getmess(ErxesRequest ER, Context context) {
        this.ER = ER;
    }

    public void run(String conversationid){
        this.conversationid = conversationid;
        ER.apolloClient.query(MessagesQuery.builder().conversationId(conversationid)
                .build()).enqueue(request);
    }
    private ApolloCall.Callback<MessagesQuery.Data> request = new ApolloCall.Callback<MessagesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<MessagesQuery.Data> response) {

            if(response.data().messages().size() > 0) {
                List<ConversationMessage> data = ConversationMessage.convert(response,conversationid);
                Realm inner = Realm.getInstance(Helper.getRealmConfig());
                inner.beginTransaction();
                inner.copyToRealmOrUpdate(data);
                inner.commitTransaction();
                inner.close();
            }
            ER.notefyAll(ReturnType.Getmessages,conversationid,null);

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED,conversationid,null);
            Log.d(TAG,"Getmessages failed ");
        }
    };
}
