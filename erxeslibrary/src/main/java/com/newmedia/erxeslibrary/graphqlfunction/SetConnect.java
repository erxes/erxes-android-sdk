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

import javax.annotation.Nonnull;


public class SetConnect {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config;
    private DataManager dataManager;
    private boolean isLogin = true;

    public SetConnect(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(String email, String phone, boolean isUser, boolean isLogin, String data) {
        this.isLogin = isLogin;
        JSONObject customData = null;
        try {
            customData = new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ER.apolloClient.mutate(MessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
                .data(new Json(customData))
                .build()).enqueue(request);
    }

    private ApolloCall.Callback<MessengerConnectMutation.Data> request = new ApolloCall.Callback<MessengerConnectMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {

                config.customerId = response.data().messengerConnect().customerId();
                config.integrationId = response.data().messengerConnect().integrationId();

                dataManager.setData(DataManager.customerId, config.customerId);
                dataManager.setData(DataManager.integrationId, config.integrationId);

                config.changeLanguage(response.data().messengerConnect().languageCode());
                Helper.load_uiOptions(response.data().messengerConnect().uiOptions());
                Helper.load_messengerData(response.data().messengerConnect().messengerData());

                if (!isLogin)
                    getLead();

                ER.notefyAll(ReturnType.LOGIN_SUCCESS, null, null);
            } else {

                Log.d(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.d(TAG, "failed ");
            e.printStackTrace();

        }
    };

    private void getLead() {
        if (!TextUtils.isEmpty(config.messengerdata.getFormCode()))
            ER.apolloClient.mutate(FormConnectMutation.builder()
                    .brandCode(config.brandCode)
                    .formCode(config.messengerdata.getFormCode())
                    .build()).enqueue(formConnect);
    }

    private ApolloCall.Callback<FormConnectMutation.Data> formConnect = new ApolloCall.Callback<FormConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<FormConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.formConnect = FormConnect.convert(response);
                ER.notefyAll(ReturnType.LEAD, null, null);
            } else {
                Log.e(TAG, "onResponse: " + response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            Log.e(TAG, "onFailure: formConnect");
        }
    };
}
