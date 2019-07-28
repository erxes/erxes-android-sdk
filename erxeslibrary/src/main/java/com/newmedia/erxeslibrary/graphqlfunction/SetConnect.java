package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FormConnectMutation;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.helper.Json;
import com.newmedia.erxeslibrary.model.FormConnect;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;


public class SetConnect {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config;
    private DataManager dataManager;

    public SetConnect(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(String email, String phone, boolean isUser, String data) {
        JSONObject customData = new JSONObject();
        try {
            if (data != null) {
                customData = new JSONObject(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ER.apolloClient.mutate(MessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
                .data(new Json(customData))
                .build()
        ).enqueue(request);
    }

    private ApolloCall.Callback<MessengerConnectMutation.Data> request = new ApolloCall.Callback<MessengerConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {

                config.customerId = response.data().messengerConnect().customerId();
                config.integrationId = response.data().messengerConnect().integrationId();

                dataManager.setData(DataManager.customerId, config.customerId);
                dataManager.setData(DataManager.integrationId, config.integrationId);

                config.changeLanguage(response.data().messengerConnect().languageCode());
                Helper.load_uiOptions(response.data().messengerConnect().uiOptions());
                Helper.load_messengerData(response.data().messengerConnect().messengerData());

//                getLead();

                ER.notefyAll(ReturnType.LOGIN_SUCCESS, null, null);
            } else {

                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {

            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.e(TAG, "failed " + e.getMessage() + "\n" + e.getLocalizedMessage());
            e.printStackTrace();

        }
    };


}
