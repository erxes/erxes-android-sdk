package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.erxes.io.opens.KnowledgeBaseTopicDetailQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GetKnowledge {
    final static String TAG = "GetKnowledge";
    private final ErxesRequest erxesRequest;
    private final Config config;

    public GetKnowledge(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null) {
            Rx3Apollo.from(erxesRequest.apolloClient
                    .query(KnowledgeBaseTopicDetailQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Response<KnowledgeBaseTopicDetailQuery.Data>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Response<KnowledgeBaseTopicDetailQuery.Data> response) {
                            if (!response.hasErrors()) {
                                config.knowledgeBaseTopic = KnowledgeBaseTopic.convert(response.getData());
                                erxesRequest.notefyAll(ReturntypeUtil.FAQ, null, null,null);
                            } else {
                                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.getErrors().get(0).getMessage(),null);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            erxesRequest.notefyAll(ReturntypeUtil.CONNECTIONFAILED,null,e.getMessage(),null);

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }
}
