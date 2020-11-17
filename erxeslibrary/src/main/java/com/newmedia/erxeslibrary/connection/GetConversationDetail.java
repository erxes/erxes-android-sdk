package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsConversationDetailQuery;
import com.erxes.io.opens.WidgetsMessengerSupportersQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetConversationDetail {
    final static String TAG = "GetConversationDetail";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public GetConversationDetail(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        WidgetsConversationDetailQuery query = WidgetsConversationDetailQuery.builder()
                .id(config.conversationId)
                .integ(config.integrationId).build();
        Rx3Apollo.from(erxesRequest.apolloClient
                .query(query))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsConversationDetailQuery.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsConversationDetailQuery.Data> response) {
                        if (!response.hasErrors()) {
                            if (response.getData().widgetsConversationDetail().isOnline() != null)
                                config.isOnline = response.getData().widgetsConversationDetail().isOnline();
                            List<User> participatedUsers = User.convertParticipatedUsers(response.getData().widgetsConversationDetail().participatedUsers());

                            erxesRequest.notefyAll(ReturntypeUtil.GETCONVERSATIONDETAIL, null, null,participatedUsers);
                        } else {
                            if (response.getErrors() != null)
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
}
