package com.newmedia.erxeslibrary.connection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.WidgetBotRequestMutation;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.erxes.io.opens.type.AttachmentInput;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class WidgetBotRequest {
    final static String TAG = "widgetBotRequest";
    private final ErxesRequest erxesRequest;
    private final Config config;
    private final Context context;

    public WidgetBotRequest(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        this.context = context;
        config = Config.getInstance(context);
    }

    public void run(String mContent, String type,String payload) {
        if (TextUtils.isEmpty(mContent) ) {
            mContent = "This message has an attachment";
        }
        WidgetBotRequestMutation.Builder temp = WidgetBotRequestMutation.builder()
                .integrationId(config.integrationId)
                .customerId(config.customerId)
                .message(mContent)
                .conversationId(config.conversationId)
                .payload(payload)
                .type(type);

        String finalMContent = mContent;
        Rx3Apollo.from(erxesRequest.apolloClient
                .mutate(temp.build()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Response<WidgetBotRequestMutation.Data>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<WidgetBotRequestMutation.Data> response) {
                        if (response.hasErrors()) {
                            erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, config.conversationId, response.getErrors().get(0).getMessage(),null);
                        } else {
                            if (response.getData() != null) {
                                Log.d("fuck","  xx  "+response.getData().widgetBotRequest().toString());
                                erxesRequest.notefyAll(ReturntypeUtil.GETBOTINITIALMESSAGE, config.conversationId, null, response.getData().widgetBotRequest());
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
