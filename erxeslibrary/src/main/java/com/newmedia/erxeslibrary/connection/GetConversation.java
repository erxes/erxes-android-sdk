package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsConversationsQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.Conversation;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetConversation {
    final static String TAG = "GetConversation";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetConversation(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        Rx3Apollo.from(erxesRequest.apolloClient
                .query(WidgetsConversationsQuery.builder()
                        .integrationId(config.integrationId)
                        .customerId(config.customerId).build())
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsConversationsQuery.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsConversationsQuery.Data> response) {
                        if (response.getData() != null && response.getData().widgetsConversations() != null && response.getData().widgetsConversations().size() > 0) {
                            if (config.conversations != null && config.conversations.size() > 0)
                                config.conversations.clear();
                            if (config.conversations != null) {
                                for (Conversation conversation : Conversation.convert(response, config)) {
                                    if (conversation.status.equalsIgnoreCase("open"))
                                        config.conversations.add(conversation);
                                }
                            }
                            erxesRequest.notefyAll(ReturntypeUtil.GETCONVERSATION, null, null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED, null, e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
