package com.newmedia.erxeslibrary.connection;

import android.util.Log;
import android.content.Context;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsSaveLeadMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.Json;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SendLead {
    final static String TAG = "SendLead";
    private ErxesRequest erxesRequest;
    private Config config;

    public SendLead(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        Map browserInfo = new HashMap();
        WidgetsSaveLeadMutation mutate = WidgetsSaveLeadMutation.builder()
                .formId(config.formConnect.getLead().getId())
                .integrationId(config.integrationId)
                .submissions(config.fieldValueInputs)
                .browserInfo(new Json(browserInfo))
                .build();
        Rx3Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsSaveLeadMutation.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsSaveLeadMutation.Data> response) {
                        if (!response.hasErrors()) {
                            if (response.data().widgetsSaveLead().status().equalsIgnoreCase("ok")) {
                                erxesRequest.notefyAll(ReturntypeUtil.SAVEDLEAD, null, response.getData().widgetsSaveLead().status(),null);
                            } else {
                                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.getData().widgetsSaveLead().status(),null);
                            }
                        } else {
                            erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.getErrors().get(0).getMessage(),null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED,null,e.getMessage(),null);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
