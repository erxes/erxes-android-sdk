package com.newmedia.erxeslibrary.graphqlfunction;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.newmedia.erxes.basic.FormConnectMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.model.FormConnect;

import org.jetbrains.annotations.NotNull;

public class GetLead {

    final static String TAG = "GetLead";
    private ErxesRequest erxesRequest;
    private Config config;

    public GetLead(ErxesRequest erxesRequest, Context context) {
        this.erxesRequest = erxesRequest;
        config = Config.getInstance(context);
    }

    public void run() {
        if (!TextUtils.isEmpty(config.messengerdata.getFormCode()))
            erxesRequest.apolloClient.mutate(FormConnectMutation.builder()
                    .brandCode(config.brandCode)
                    .formCode(config.messengerdata.getFormCode())
                    .build()).enqueue(formConnect);
    }

    private ApolloCall.Callback<FormConnectMutation.Data> formConnect = new ApolloCall.Callback<FormConnectMutation.Data>() {
        @Override
        public void onResponse(@NotNull Response<FormConnectMutation.Data> response) {
            if (!response.hasErrors()) {
                config.formConnect = FormConnect.convert(response);
                erxesRequest.notefyAll(Returntype.LEAD, null, null);
            } else {
                Log.e(TAG, "onResponse: " + response.errors().get(0).message());
            }
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            e.printStackTrace();
            Log.e(TAG, "onFailure: formConnect");
        }
    };
}
