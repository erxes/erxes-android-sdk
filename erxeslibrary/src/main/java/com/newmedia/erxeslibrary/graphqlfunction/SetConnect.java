package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.FormConnectMutation;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxes.basic.SaveLeadMutation;
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

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


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
        MessengerConnectMutation mutate = MessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
                .data(new Json(customData))
                .build();
//        ER.apolloClient.mutate(mutate).enqueue(request);
        Rx2Apollo.from(ER.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a);
    }
    private Observer a = new Observer<Response<MessengerConnectMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {
//                if (!config.messengerdata.isShowLauncher()) {

                config.customerId = response.data().messengerConnect().customerId();
                config.integrationId = response.data().messengerConnect().integrationId();

                dataManager.setData(DataManager.customerId, config.customerId);
                dataManager.setData(DataManager.integrationId, config.integrationId);

                config.changeLanguage(response.data().messengerConnect().languageCode());
                Helper.load_uiOptions(response.data().messengerConnect().uiOptions());
                Helper.load_messengerData(response.data().messengerConnect().messengerData());

                ER.notefyAll(ReturnType.LOGIN_SUCCESS, null, null);
//                }
            } else {
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };
    private ApolloCall.Callback<MessengerConnectMutation.Data> request = new ApolloCall.Callback<MessengerConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {
//                if (!config.messengerdata.isShowLauncher()) {
                    config.customerId = response.data().messengerConnect().customerId();
                    config.integrationId = response.data().messengerConnect().integrationId();

                    dataManager.setData(DataManager.customerId, config.customerId);
                    dataManager.setData(DataManager.integrationId, config.integrationId);

                    config.changeLanguage(response.data().messengerConnect().languageCode());
                    Helper.load_uiOptions(response.data().messengerConnect().uiOptions());
                    Helper.load_messengerData(response.data().messengerConnect().messengerData());
                    ER.notefyAll(ReturnType.LOGIN_SUCCESS, null, null);
//                }
            } else {
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            e.printStackTrace();
        }
    };
}
