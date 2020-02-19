package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsMessagesQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetMessage {
    final static String TAG = "GetMessage";
    private ErxesRequest erxesRequest;
    private String conversationid;
    private Config config;

    public GetMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        this.conversationid = conversationid;
        WidgetsMessagesQuery query = WidgetsMessagesQuery.builder()
                .conversationId(conversationid)
                .build();
        Rx2Apollo.from(erxesRequest.apolloClient
                .query(query)
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
    private Observer observer = new Observer<Response<WidgetsMessagesQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<WidgetsMessagesQuery.Data> response) {
            if (response.data().widgetsMessages().size() > 0) {
                if (config.conversationMessages.size() > 0)
                    config.conversationMessages.clear();
                List<ConversationMessage> conversationMessages = ConversationMessage.convert(response, conversationid);
                for (ConversationMessage message : conversationMessages) {
                    if (!message.internal)
                        config.conversationMessages.add(message);
                }

                erxesRequest.notefyAll(ReturntypeUtil.GETMESSAGES, conversationid, null);
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
