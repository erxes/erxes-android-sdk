package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsGetMessengerIntegrationQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetIntegration {
    final static String TAG = "GetIntegration";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public GetIntegration(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);

    }

    public void run() {

        Rx3Apollo.from(erxesRequest.apolloClient
                .query(WidgetsGetMessengerIntegrationQuery.builder().brandCode(config.brandCode)
                        .build()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsGetMessengerIntegrationQuery.Data>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.e(TAG,"onsubscribe");
                    }

                    @Override
                    public void onNext(@NonNull Response<WidgetsGetMessengerIntegrationQuery.Data> dataResponse) {
                        if (!dataResponse.hasErrors()) {
                            try {
                                config.changeLanguage(dataResponse.getData().widgetsGetMessengerIntegration().languageCode());
                                ErxesHelper.load_uiOptions(dataResponse.getData().widgetsGetMessengerIntegration().uiOptions());
                                ErxesHelper.load_messengerData(dataResponse.getData().widgetsGetMessengerIntegration().messengerData());
                                config.initActivity();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "errors " + dataResponse.getErrors().toString());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG,"onComplete");
                    }
                });
    }
}
