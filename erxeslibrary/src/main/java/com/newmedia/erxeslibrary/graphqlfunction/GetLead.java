package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxes.basic.FormConnectMutation;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.FormConnect;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetLead {

    final static String TAG = "GetLead";
    private ErxesRequest ER;
    private Config config;
    private DataManager dataManager;

    public GetLead(ErxesRequest ER, Activity context) {
        this.ER = ER;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run() {
        if (!TextUtils.isEmpty(config.messengerdata.getFormCode())) {
//            ER.apolloClient.mutate(FormConnectMutation.builder()
//                    .brandCode(config.brandCode)
//                    .formCode(config.messengerdata.getFormCode())
//                    .build()).enqueue(formConnect);
            Rx2Apollo.from(ER.apolloClient
                    .mutate(FormConnectMutation.builder()
                            .brandCode(config.brandCode)
                            .formCode(config.messengerdata.getFormCode())
                            .build()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(a);
        }
    }
    private Observer a = new Observer<Response<FormConnectMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<FormConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.formConnect = FormConnect.convert(response);
                ER.notefyAll(ReturnType.LEAD, null, null);
                Log.e(TAG, "no Error");

            } else {
                Log.e(TAG, "onResponse: " + response.errors().get(0).message());
            }
            ER.notefyAll(ReturnType.Getconversation,null,null);
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
    private ApolloCall.Callback<FormConnectMutation.Data> formConnect = new ApolloCall.Callback<FormConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<FormConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.formConnect = FormConnect.convert(response);
                ER.notefyAll(ReturnType.LEAD, null, null);
                Log.e(TAG, "no Error");

            } else {
                Log.e(TAG, "onResponse: " + response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            Log.e(TAG, "onFailure: formConnect");
        }
    };
}
