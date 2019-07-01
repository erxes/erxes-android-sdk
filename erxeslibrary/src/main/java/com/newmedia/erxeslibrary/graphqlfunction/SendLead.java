package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.SaveLeadMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.helper.Json;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

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
        ER.apolloClient.mutate(SaveLeadMutation.builder()
                .formId(config.messengerdata.getFormCode())
                .integrationId(config.integrationId)
                .submissions(config.fieldValueInputs)
                .browserInfo(new Json(new JSONObject()))
                .build())
                .enqueue(request);
    }

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
