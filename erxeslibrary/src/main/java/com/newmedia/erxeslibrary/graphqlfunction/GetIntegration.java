package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ErxesHelper;
import org.json.JSONObject;

public class GetIntegration {
    final static String TAG = "GETINTEG";
    private ErxesRequest erxesRequest;
    private Config config;
    private boolean hasData;
    private String email, phone;
    private JSONObject jsonObject;

    public GetIntegration(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);

    }

    public void run(boolean hasData, String email, String phone, JSONObject jsonObject) {
        this.hasData = hasData;
        this.email = email;
        this.phone = phone;
        this.jsonObject = jsonObject;
        erxesRequest.apolloClient.query(GetMessengerIntegrationQuery.builder()
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
                    ErxesHelper.load_uiOptions(response.data().getMessengerIntegration().uiOptions());
                    ErxesHelper.load_messengerData(response.data().getMessengerIntegration().messengerData());
                    if (config.messengerdata != null) {
                        if (!config.messengerdata.isShowLauncher()) {
                            erxesRequest.setConnect(email, phone, true, jsonObject.toString());
                        } else {
                            config.initActivity(hasData, email, phone, jsonObject);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(ApolloException e) {
            e.printStackTrace();
        }
    };
}
