package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.SaveLeadMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.helper.Json;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class SendLead {
    final static String TAG = "SendLead";
    private ErxesRequest erxesRequest;
    private Config config;

    public SendLead(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        erxesRequest.apolloClient.mutate(SaveLeadMutation.builder()
                .formId(config.formConnect.getLead().getId())
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
                    erxesRequest.notefyAll(Returntype.SAVEDLEAD, null, response.data().saveForm().status());
                } else {
                    erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.data().saveForm().status());
                }
            } else {
                erxesRequest.notefyAll(Returntype.SERVERERROR, null, response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            erxesRequest.notefyAll(Returntype.CONNECTIONFAILED, null, e.getMessage());
            e.printStackTrace();
        }
    };
}
