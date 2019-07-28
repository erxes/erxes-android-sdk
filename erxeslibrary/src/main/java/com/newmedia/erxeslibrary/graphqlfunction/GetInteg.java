package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;

import org.jetbrains.annotations.NotNull;

public class GetInteg {
    final static String TAG = "GETINTEG";
    private ErxesRequest ER;
    private Config config;

    public GetInteg(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);

    }

    public void run() {
        Log.e(TAG, "run: " + config.brandCode );
        ER.apolloClient.query(GetMessengerIntegrationQuery.builder()
                .brandCode(config.brandCode)
                .build()
        ).enqueue(request);
    }

    private ApolloCall.Callback<GetMessengerIntegrationQuery.Data> request = new ApolloCall.Callback<GetMessengerIntegrationQuery.Data>() {
        @Override
        public void onResponse(Response<GetMessengerIntegrationQuery.Data> response) {
            if (!response.hasErrors()) {
                try {
                    config.changeLanguage(response.data().getMessengerIntegration().languageCode());
                    Helper.load_uiOptions(response.data().getMessengerIntegration().uiOptions());
                    Helper.load_messengerData(response.data().getMessengerIntegration().messengerData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ER.notefyAll(ReturnType.INTEGRATION_CHANGED, null, null);
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.d(TAG, "failed ");
            e.printStackTrace();

        }
    };

}
