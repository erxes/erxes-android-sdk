package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.erxes.io.opens.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.connection.service.SaasListenerService;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.connection.service.ListenerService;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InsertNewMessage {
    final static String TAG = "insertnew";
    private ErxesRequest erxesRequest;
    private Config config;
    private Context context;
    private String mContent;
    private DataManager dataManager;

    public InsertNewMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        this.context = context;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run(String mContent, List<AttachmentInput> list) {
        this.mContent = mContent;
        if (TextUtils.isEmpty(this.mContent)) {
            this.mContent = "This message has an attachment";
        }
        WidgetsInsertMessageMutation mutate = WidgetsInsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(this.mContent)
                .conversationId("")
                .attachments(list)
                .build();

        Rx2Apollo.from(erxesRequest.apolloClient
                .mutate(mutate))
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
                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.errors().get(0).message());
            } else {
                Conversation conversation = Conversation.update(response.data().widgetsInsertMessage(), mContent, config);
                ConversationMessage a = ConversationMessage.convert(response.data().widgetsInsertMessage(), mContent, config);
                config.conversations.add(conversation);
                config.conversationMessages.add(a);

                config.intent.putExtra("id", config.conversationId);
                context.startService(config.intent);

                erxesRequest.notefyAll(ReturntypeUtil.MUTATIONNEW, response.data().widgetsInsertMessage().fragments().messageFragment().conversationId(), null);
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
