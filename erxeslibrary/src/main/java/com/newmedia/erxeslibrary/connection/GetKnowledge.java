package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.erxes.io.opens.KnowledgeBaseTopicDetailQuery;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GetKnowledge {
    final static String TAG = "GetKnowledge";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetKnowledge(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (config.messengerdata != null && config.messengerdata.getKnowledgeBaseTopicId() != null) {
            Rx2Apollo.from(erxesRequest.apolloClient
                    .query(KnowledgeBaseTopicDetailQuery.builder().topicId(config.messengerdata.getKnowledgeBaseTopicId()).build())
//                    .httpCachePolicy(HttpCachePolicy.CACHE_FIRST)
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(observer);
        }
    }
    private Observer observer = new Observer<Response<KnowledgeBaseTopicDetailQuery.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<KnowledgeBaseTopicDetailQuery.Data> response) {
            if (!response.hasErrors()) {
                config.knowledgeBaseTopic = KnowledgeBaseTopic.convert(response.data());
                erxesRequest.notefyAll(ReturntypeUtil.FAQ, null, null);
            } else {
                erxesRequest.notefyAll(ReturntypeUtil.SERVERERROR, null, response.errors().get(0).message());
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
