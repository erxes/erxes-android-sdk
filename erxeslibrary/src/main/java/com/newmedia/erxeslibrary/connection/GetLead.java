package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.text.TextUtils;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsLeadConnectMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.FormConnect;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetLead {

    final static String TAG = "GetLead";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetLead(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (!TextUtils.isEmpty(config.messengerdata.getFormCode())) {
            Rx2Apollo.from(erxesRequest.apolloClient
                    .mutate(WidgetsLeadConnectMutation.builder()
                            .brandCode(config.brandCode)
                            .formCode(config.messengerdata.getFormCode())
                            .build()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }
    private Observer observer = new Observer<Response<WidgetsLeadConnectMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<WidgetsLeadConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.formConnect = FormConnect.convert(response);
                erxesRequest.notefyAll(ReturntypeUtil.LEAD, null, null);
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED,null,e.getMessage());
        }

        @Override
        public void onComplete() {

        }
    };
}
