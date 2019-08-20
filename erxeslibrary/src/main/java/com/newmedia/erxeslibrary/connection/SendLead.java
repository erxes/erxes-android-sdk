package com.newmedia.erxeslibrary.connection;

import android.util.Log;
import android.content.Context;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.SaveLeadMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.Returntype;
import com.newmedia.erxeslibrary.helper.Json;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SendLead {
    final static String TAG = "SendLead";
    private ErxesRequest erxesRequest;
    private Config config;

    public SendLead(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        SaveLeadMutation mutate = SaveLeadMutation.builder()
                .formId(config.formConnect.getLead().getId())
                .integrationId(config.integrationId)
                .submissions(config.fieldValueInputs)
                .browserInfo(new Json(new JSONObject()))
                .build();
        Rx2Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<SaveLeadMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<SaveLeadMutation.Data> response) {
            if (!response.hasErrors()) {
                if (response.data().saveForm().status().equalsIgnoreCase("ok")) {
                    erxesRequest.notefyAll(Returntype.SAVEDLEAD, null, response.data().saveForm().status());
                } else {
                    erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.data().saveForm().status());
                }
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED,null,e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };
}
