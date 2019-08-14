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
import org.json.JSONObject;

public class GetInteg {
    final static String TAG = "GETINTEG";
    private ErxesRequest ER;
    private Config config;
    private boolean hasData;
    private String email, phone;
    private JSONObject jsonObject;

    public GetInteg(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);

    }

    public void run(boolean hasData, String email, String phone, JSONObject jsonObject) {
        this.hasData = hasData;
        this.email = email;
        this.phone = phone;
        this.jsonObject = jsonObject;
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
                    if (config.messengerdata != null) {
                        if (!config.messengerdata.isShowLauncher()) {
                            ER.setConnect(email, phone, true, jsonObject.toString());
                        } else {
                            config.initActivity(hasData, email, phone, jsonObject);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
            }
        }

        @Override
        public void onFailure(ApolloException e) {
            Log.e(TAG, "failed ");
            e.printStackTrace();
        }
    };
}
