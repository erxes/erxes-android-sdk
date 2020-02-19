package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsConversationsQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.Conversation;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetConversation {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetConversation(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        Rx2Apollo.from(erxesRequest.apolloClient
                .query(WidgetsConversationsQuery.builder()
                        .integrationId(config.integrationId)
                        .customerId(config.customerId).build())
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observer observer = new Observer<Response<WidgetsConversationsQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<WidgetsConversationsQuery.Data> response) {
            if (response.data().widgetsConversations().size() > 0) {
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
    };
}
