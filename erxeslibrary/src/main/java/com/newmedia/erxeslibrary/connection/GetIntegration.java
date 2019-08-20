package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;

import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

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

        Rx2Apollo.from(erxesRequest.apolloClient
                .query(GetMessengerIntegrationQuery.builder().brandCode(config.brandCode)
                        .build())
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<GetMessengerIntegrationQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {
            Log.e(TAG,"onsubscribe");
        }

        @Override
        public void onNext(Response<GetMessengerIntegrationQuery.Data> response) {
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
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();

        }

        @Override
        public void onComplete() {
            Log.e(TAG,"onComplete");
        }
    };
}
