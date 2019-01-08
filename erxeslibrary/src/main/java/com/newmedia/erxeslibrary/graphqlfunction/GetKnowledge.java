package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.DB;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.Configuration.ReturnType;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import javax.annotation.Nonnull;

public class GetKnowledge {
    final static String TAG = "GetKnowledge";
    private ErxesRequest ER;
    private Config config ;
    public GetKnowledge(ErxesRequest ER, Context context) {
        this.ER = ER;
        config = Config.getInstance(context);

    }
    public void run(){

        if(config.messengerdata != null && config.messengerdata.knowledgeBaseTopicId != null){
            ER.apolloClient.query(FaqGetQuery.builder().topicId(config.messengerdata.knowledgeBaseTopicId).build())
                    .enqueue(request);
        }


    }

    private ApolloCall.Callback<FaqGetQuery.Data> request =  new ApolloCall.Callback<FaqGetQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<FaqGetQuery.Data> response) {
            if(!response.hasErrors()) {
                KnowledgeBaseTopic a = new KnowledgeBaseTopic();
                a.convert(response.data());
                DB.save(a);
//              ER.notefyAll(ReturnType.INTEGRATION_CHANGED,null ,null);
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
