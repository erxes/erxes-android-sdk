package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
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
import com.newmedia.erxeslibrary.model.FormConnect;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;



public class SetConnect {
    final static String TAG = "SETCONNECT";
    private ErxesRequest ER;
    private Config config ;
    private DataManager dataManager;
    private boolean isLogin = true;

    public SetConnect(ErxesRequest ER, Context context) {
        this.ER = ER;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }
    public void run(String email,String phone,boolean isUser, boolean isLogin){
        this.isLogin = isLogin;
        ER.apolloClient.mutate(MessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
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
                Log.e(TAG,response.data().messengerConnect().messengerData().toString());
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
        ER.apolloClient.mutate(FormConnectMutation.builder()
                .brandCode(config.brandCode)
                .formCode(config.messengerdata.getFormCode())
                .build()).enqueue(formConnect);
    }

    private ApolloCall.Callback<FormConnectMutation.Data> formConnect = new ApolloCall.Callback<FormConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<FormConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                Log.e(TAG, "onResponse: Lead");
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
