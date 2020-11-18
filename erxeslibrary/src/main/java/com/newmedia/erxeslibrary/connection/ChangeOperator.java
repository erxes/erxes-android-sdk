package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.ChangeConversationOperatorMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChangeOperator {
    final static String TAG = "ChangeOperator";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public ChangeOperator(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        ChangeConversationOperatorMutation mutate = ChangeConversationOperatorMutation.builder()
                ._id(conversationid)
                .operatorStatus("operator")
                .build();
        Rx3Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<ChangeConversationOperatorMutation.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ChangeConversationOperatorMutation.Data> response) {
                        if (response.hasErrors()) {
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
