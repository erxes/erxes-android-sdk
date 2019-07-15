package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.MessengerSupportersQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.User;

import org.jetbrains.annotations.NotNull;

public class GetSup {
    final static String TAG = "GETSUP";
    private ErxesRequest ER;
    private Config config;
    private Activity context;

    public GetSup(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);
        this.context = context;
    }

    public void run() {
        ER.apolloClient.query(MessengerSupportersQuery.builder().integ(config.integrationId).build())
                .enqueue(request);
    }

    private ApolloCall.Callback<MessengerSupportersQuery.Data> request = new ApolloCall.Callback<MessengerSupportersQuery.Data>() {
        @Override
        public void onResponse(@NotNull final Response<MessengerSupportersQuery.Data> response) {
            if (!response.hasErrors()) {
                if (config.supporters != null && config.supporters.size() > 0)
                    config.supporters.clear();
                if (config.supporters != null) {
                    config.supporters.addAll(User.convert(response.data().messengerSupporters()));
                }
                ER.notefyAll(ReturnType.GetSupporters, null, null);
            } else {
                Log.d(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.d(TAG, "failed ");
            e.printStackTrace();

        }
    };
}
