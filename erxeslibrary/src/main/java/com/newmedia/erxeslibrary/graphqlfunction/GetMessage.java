package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetMessage {
    final static String TAG = "SETCONNECT";
    private ErxesRequest erxesRequest;
    private String conversationid;
    private Config config;

    public GetMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        this.conversationid = conversationid;
        MessagesQuery query = MessagesQuery.builder()
                .conversationId(conversationid)
                .build();
        Rx2Apollo.from(erxesRequest.apolloClient
                .query(query)
                .httpCachePolicy(HttpCachePolicy.CACHE_FIRST))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<MessagesQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<MessagesQuery.Data> response) {
            if (response.data().messages().size() > 0) {
                if (config.conversationMessages.size() > 0)
                    config.conversationMessages.clear();
                List<ConversationMessage> conversationMessages = ConversationMessage.convert(response, conversationid);
                for (ConversationMessage message : conversationMessages) {
                    if (!message.internal)
                        config.conversationMessages.add(message);
                }

                erxesRequest.notefyAll(Returntype.GETMESSAGES, conversationid, null);
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
