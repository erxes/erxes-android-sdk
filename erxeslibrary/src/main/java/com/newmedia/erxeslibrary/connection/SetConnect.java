package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.helper.Json;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class SetConnect {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private Config config;
    private DataManager dataManager;
    private boolean isCheckRequired, hasData, isUser;
    private String email, phone, customData;

    public SetConnect(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(boolean isCheckRequired, boolean isUser, boolean hasData, String email, String phone, String data) {
        this.isCheckRequired = isCheckRequired;
        this.hasData = hasData;
        this.email = email;
        this.phone = phone;
        this.customData = data;
        this.isUser = isUser;
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
        Rx2Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observer observer = new Observer<Response<MessengerConnectMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<MessengerConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                ErxesHelper.load_messengerData(response.data().messengerConnect().messengerData());
                if (isCheckRequired) {
                    if (config.messengerdata != null) {
                        if (config.messengerdata.isShowLauncher()) {
                            prepareData(response);
                            config.initActivity(hasData, email, phone, customData);
                        } else {
                            erxesRequest.setConnect(!isCheckRequired, isUser, hasData, email, phone, customData);
                        }
                    }
                } else {
                    if (config.messengerdata != null && config.messengerdata.isShowLauncher()) {
                        prepareData(response);
                        erxesRequest.notefyAll(ReturntypeUtil.LOGINSUCCESS, null, null);
                    }
                }
            } else {
                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED, null, e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };

    private void prepareData(Response<MessengerConnectMutation.Data> response) {
        config.customerId = response.data().messengerConnect().customerId();
        config.integrationId = response.data().messengerConnect().integrationId();

        dataManager.setData(DataManager.CUSTOMERID, config.customerId);
        dataManager.setData(DataManager.INTEGRATIONID, config.integrationId);

        config.changeLanguage(response.data().messengerConnect().languageCode());
        ErxesHelper.load_uiOptions(response.data().messengerConnect().uiOptions());
    }
}
