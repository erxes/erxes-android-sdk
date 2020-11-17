package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsMessengerConnectMutation;
import com.google.gson.Gson;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.helper.Json;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.utils.DataManager;

import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class SetConnect {
    final static String TAG = "SETCONNECT";
    private final ErxesRequest erxesRequest;
    private final Config config;
    private final DataManager dataManager;
    private String customData;

    public SetConnect(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(boolean isCheckRequired, boolean isUser, boolean hasData, String email, String phone, String data) {
        this.customData = data;
        Gson gson = new Gson();
        Map customDataMap = gson.fromJson(data, Map.class);

        WidgetsMessengerConnectMutation mutate = WidgetsMessengerConnectMutation.builder()
                .brandCode(config.brandCode)
                .email(email)
                .phone(phone)
                .isUser(isUser)
                .data(new Json(customDataMap))
                .build();
        Rx3Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsMessengerConnectMutation.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsMessengerConnectMutation.Data> response) {
                        if (!response.hasErrors()) {
                            ErxesHelper.load_messengerData(response.getData().widgetsMessengerConnect().messengerData());
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
                                    erxesRequest.notefyAll(ReturntypeUtil.LOGINSUCCESS, null, null,null);
                                }
                            }
                        } else {
                            erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.getErrors().get(0).getMessage(),null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED, null, e.getMessage(),null);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    private void prepareData(Response<WidgetsMessengerConnectMutation.Data> response) {
        config.customerId = response.getData().widgetsMessengerConnect().customerId();
        config.integrationId = response.getData().widgetsMessengerConnect().integrationId();
        if (response.getData().widgetsMessengerConnect().brand() != null) {
            config.brandName = response.getData().widgetsMessengerConnect().brand().name();
            config.brandDescription = response.getData().widgetsMessengerConnect().brand().description();
        }

        dataManager.setData(DataManager.CUSTOMERID, config.customerId);
        dataManager.setData(DataManager.INTEGRATIONID, config.integrationId);

        config.changeLanguage(response.getData().widgetsMessengerConnect().languageCode());
        ErxesHelper.load_uiOptions(response.getData().widgetsMessengerConnect().uiOptions());
    }
}
