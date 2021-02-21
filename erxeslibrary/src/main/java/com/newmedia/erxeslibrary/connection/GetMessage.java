package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsMessagesQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetMessage {
    final static String TAG = "GetMessage";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public GetMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String conversationid) {
        WidgetsMessagesQuery query = WidgetsMessagesQuery.builder()
                .conversationId(conversationid)
                .build();
        Rx3Apollo.from(erxesRequest.apolloClient
                .query(query))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsMessagesQuery.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsMessagesQuery.Data> response) {
                        if (response.getData().widgetsMessages().size() > 0) {
                            List<ConversationMessage> conversationMessages = ConversationMessage.convert(response, conversationid);
                            List<ConversationMessage> withoutBotList = new ArrayList<>();
                            for (ConversationMessage message : conversationMessages) {
//                                if (!message.internal && message.botData == null) {
                                    if (!message.internal ) {
                                    withoutBotList.add(message);
                                }
                            }
                            erxesRequest.notefyAll(ReturntypeUtil.GETMESSAGES, conversationid, null, withoutBotList);
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
