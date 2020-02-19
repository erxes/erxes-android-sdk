package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.erxes.io.opens.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InsertMessage {
    final static String TAG = "InsertMessage";
    private ErxesRequest erxesRequest;
    private Config config;
    private String conversationId, mContent;

    public InsertMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run(String mContent, final String conversationId, List<AttachmentInput> list) {
        this.mContent = mContent;
        if (TextUtils.isEmpty(this.mContent)) {
            this.mContent = "This message has an attachment";
        }

        this.conversationId = conversationId;
        WidgetsInsertMessageMutation.Builder temp = WidgetsInsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(this.mContent)
                .attachments(list)
                .conversationId(conversationId);

        Rx2Apollo.from(erxesRequest.apolloClient
                .mutate(temp.build()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Observer observer = new Observer<Response<WidgetsInsertMessageMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<WidgetsInsertMessageMutation.Data> response) {
            if (response.hasErrors()) {
                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, conversationId, response.errors().get(0).message());
            } else {
                ConversationMessage a = ConversationMessage.convert(response.data().widgetsInsertMessage(), mContent, config);
                if (!config.conversationMessages.get(config.conversationMessages.size() - 1).id
                        .equals(a.id) && !a.internal) {
                    config.conversationMessages.add(a);
                }
                erxesRequest.notefyAll(ReturntypeUtil.MUTATION, conversationId, null);
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
