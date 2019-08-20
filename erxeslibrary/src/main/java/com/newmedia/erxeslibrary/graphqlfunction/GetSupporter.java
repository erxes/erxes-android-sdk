package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.MessengerSupportersQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.User;

import org.jetbrains.annotations.NotNull;

public class GetSupporter {
    final static String TAG = "GETSUP";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetSupporter(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        erxesRequest.apolloClient.query(MessengerSupportersQuery.builder().integ(config.integrationId).build())
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
                erxesRequest.notefyAll(Returntype.GETSUPPORTERS, null, null);
            } else {
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED, null, e.getMessage());
            e.printStackTrace();

        }
    };
}
