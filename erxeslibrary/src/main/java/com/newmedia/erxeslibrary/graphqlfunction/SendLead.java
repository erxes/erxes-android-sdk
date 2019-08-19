package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.SaveLeadMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ListenerService;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.helper.Json;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SendLead {
    final static String TAG = "SendLead";
    private ErxesRequest ER;
    private Config config;
    private Activity activity;

    public SendLead(ErxesRequest ER, Activity activity) {
        this.ER = ER;
        config = Config.getInstance(activity);
        this.activity = activity;
    }

    public void run() {
        JSONObject browserInfo = new JSONObject();
        try {
            browserInfo = new JSONObject(config.geoResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SaveLeadMutation mutate = SaveLeadMutation.builder()
                .formId(config.formConnect.getLead().getId())
                .integrationId(config.integrationId)
                .submissions(config.fieldValueInputs)
                .browserInfo(new Json(new JSONObject()))
                .build();
//        ER.apolloClient.mutate(mutate).enqueue(request);
        Rx2Apollo.from(ER.apolloClient
                .mutate(mutate))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(a);
    }
    private Observer a = new Observer<Response<SaveLeadMutation.Data>>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Response<SaveLeadMutation.Data> response) {
            if (!response.hasErrors()) {
                if (response.data().saveForm().status().equalsIgnoreCase("ok")) {
                    ER.notefyAll(ReturnType.savedLead, null, response.data().saveForm().status());
                } else {
                    ER.notefyAll(ReturnType.SERVERERROR, null, response.data().saveForm().status());
                }
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            ER.notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());

        }

        @Override
        public void onComplete() {

        }
    };
    private ApolloCall.Callback<SaveLeadMutation.Data> request = new ApolloCall.Callback<SaveLeadMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<SaveLeadMutation.Data> response) {
            if (!response.hasErrors()) {
                if (response.data().saveForm().status().equalsIgnoreCase("ok")) {
                    ER.notefyAll(ReturnType.savedLead, null, response.data().saveForm().status());
                } else {
                    ER.notefyAll(ReturnType.SERVERERROR, null, response.data().saveForm().status());
                }
            } else {
                Log.e(TAG, "errors " + response.errors().toString());
                ER.notefyAll(ReturnType.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            ER.notefyAll(ReturnType.CONNECTIONFAILED, null, e.getMessage());
            Log.e(TAG, "failed ");
            e.printStackTrace();
        }
    };
}
