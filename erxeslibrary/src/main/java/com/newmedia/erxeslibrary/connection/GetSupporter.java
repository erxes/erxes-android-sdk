package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsMessengerSupportersQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetSupporter {
    final static String TAG = "GetSupporter";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public GetSupporter(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (config.integrationId != null) {
            WidgetsMessengerSupportersQuery query = WidgetsMessengerSupportersQuery.builder()
                    .integ(config.integrationId).build();
            Rx3Apollo.from(erxesRequest.apolloClient
                    .query(query))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Response<WidgetsMessengerSupportersQuery.Data>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Response<WidgetsMessengerSupportersQuery.Data> response) {
                            if (!response.hasErrors()) {
                                if (response.getData().widgetsMessengerSupporters().isOnline() != null)
                                    config.isOnline = response.getData().widgetsMessengerSupporters().isOnline();
                                if (response.getData().widgetsMessengerSupporters().serverTime() != null)
                                    config.serverTime = config.now(Long.parseLong(response.getData().widgetsMessengerSupporters().serverTime()));

                                config.supporters.clear();
                                config.supporters.addAll(User.convert(response.getData().widgetsMessengerSupporters().supporters()));
                                erxesRequest.notefyAll(ReturntypeUtil.GETSUPPORTERS, null, null, null);
                            } else {
                                if (response.getErrors() != null)
                                    erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.getErrors().get(0).getMessage(), null);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED, null, e.getMessage(), null);

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

}
