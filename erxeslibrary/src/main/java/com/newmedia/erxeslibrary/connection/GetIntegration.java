package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsGetMessengerIntegrationQuery;
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

    public GetIntegration(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);

    }

    public void run(boolean hasData, String email, String phone, JSONObject jsonObject) {

        Rx2Apollo.from(erxesRequest.apolloClient
                .query(WidgetsGetMessengerIntegrationQuery.builder().brandCode(config.brandCode)
                        .build())
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<WidgetsGetMessengerIntegrationQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {
            Log.e(TAG,"onsubscribe");
        }

        @Override
        public void onNext(Response<WidgetsGetMessengerIntegrationQuery.Data> response) {
            if (!response.hasErrors()) {
                try {
                    config.changeLanguage(response.data().widgetsGetMessengerIntegration().languageCode());
                    ErxesHelper.load_uiOptions(response.data().widgetsGetMessengerIntegration().uiOptions());
                    ErxesHelper.load_messengerData(response.data().widgetsGetMessengerIntegration().messengerData());
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
