package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.erxes.io.opens.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InsertMessage {
    final static String TAG = "InsertMessage";
    private final ErxesRequest erxesRequest;
    private final Config config;
    private final Context context;

    public InsertMessage(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        this.context = context;
        config = Config.getInstance(context);
    }

    public void run(String mContent, List<AttachmentInput> list, String type) {
        if (TextUtils.isEmpty(mContent) && list.size() > 0) {
            mContent = "This message has an attachment";
        }

        Log.e(TAG, "run: "+config.conversationId);
        WidgetsInsertMessageMutation.Builder temp = WidgetsInsertMessageMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(mContent)
                .attachments(list)
                .contentType(type)
                .conversationId(config.conversationId);

        String finalMContent = mContent;
        Rx3Apollo.from(erxesRequest.apolloClient
                .mutate(temp.build()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetsInsertMessageMutation.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetsInsertMessageMutation.Data> response) {
                        if (response.hasErrors()) {
                            erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, config.conversationId, response.getErrors().get(0).getMessage(),null);
                        } else {
                            if (response.getData() != null) {
                                ConversationMessage conversationMessage = ConversationMessage.convert(response.getData().widgetsInsertMessage(), finalMContent, config);
                                config.conversationId = conversationMessage.conversationId;
                                erxesRequest.notefyAll(ReturntypeUtil.MUTATION, config.conversationId, null, conversationMessage);
                            }
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
