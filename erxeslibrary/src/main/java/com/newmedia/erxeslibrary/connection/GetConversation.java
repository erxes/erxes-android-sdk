package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsConversationsQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.ArrayList;
import java.util.List;

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
                            List<Conversation> conversations = Conversation.convert(response, config);
                            List<Conversation> openedConversations = new ArrayList<>();
                            config.conversationIds.clear();
                            for (Conversation conversation : conversations) {
                                if (conversation.status.equalsIgnoreCase("open")) {
                                    openedConversations.add(conversation);
                                    config.conversationIds.add(conversation.id);
                                }
                            }

                            erxesRequest.notefyAll(ReturntypeUtil.GETCONVERSATION, null, null, openedConversations);
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
