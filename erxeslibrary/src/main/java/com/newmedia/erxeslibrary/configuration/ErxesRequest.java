package com.newmedia.erxeslibrary.configuration;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxeslibrary.graphqlfunction.GetGEO;
import com.newmedia.erxeslibrary.graphqlfunction.GetKnowledge;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.graphqlfunction.GetInteg;
import com.newmedia.erxeslibrary.graphqlfunction.GetLead;
import com.newmedia.erxeslibrary.graphqlfunction.GetSup;
import com.newmedia.erxeslibrary.graphqlfunction.Getconv;
import com.newmedia.erxeslibrary.graphqlfunction.Getmess;
import com.newmedia.erxeslibrary.graphqlfunction.Insertmess;
import com.newmedia.erxeslibrary.graphqlfunction.Insertnewmess;
import com.newmedia.erxeslibrary.graphqlfunction.SendLead;
import com.newmedia.erxeslibrary.graphqlfunction.SetConnect;
import com.newmedia.erxeslibrary.helper.JsonCustomTypeAdapter2;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
//import okhttp3.logging.HttpLoggingInterceptor;

public class ErxesRequest {
    final private String TAG = "erxesrequest";

    public ApolloClient apolloClient;
    private OkHttpClient okHttpClient;
    private Activity activity;
    private List<ErxesObserver> observers;
    private Config config;

    static public ErxesRequest erxesRequest;

    static public ErxesRequest getInstance(Config config) {
        if (erxesRequest == null)
            erxesRequest = new ErxesRequest(config);
        return erxesRequest;
    }

    private ErxesRequest(Config config) {
        this.activity = config.activity;
        this.config = config;
        Helper.Init(activity);
    }

    public void set_client() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                .tlsVersions(TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.SSL_3_0)
//                .cipherSuites(
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
//                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
//                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA)
//                .build();

        if (config.HOST_3100 != null) {
            okHttpClient = new OkHttpClient.Builder()
//                    .connectionSpecs(Collections.singletonList(spec))
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new AddCookiesInterceptor(this.activity))
                    .addInterceptor(new ReceivedCookiesInterceptor(this.activity))
                    .build();
            apolloClient = ApolloClient.builder()
                    .serverUrl(config.HOST_3100)
                    .okHttpClient(okHttpClient)
                    .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.HOST_3300, okHttpClient))
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter2())
                    .build();
        }
    }

    public void setConnect(String email, String phone, boolean isUser, String data) {
        if (!isNetworkConnected()) {
            return;
        }
        SetConnect setConnect = new SetConnect(this, activity);
        setConnect.run(email, phone, isUser, data);
    }

    public void getGEO() {
        if (!isNetworkConnected()) {
            return;
        }
        GetGEO getGEO = new GetGEO(this, activity);
        getGEO.run();
    }

    public void getIntegration() {
        if (!isNetworkConnected()) {
            return;
        }
        GetInteg getIntegration = new GetInteg(this, activity);
        getIntegration.run();
    }

    public void InsertMessage(String message, String conversationId, List<AttachmentInput> list) {
        if (!isNetworkConnected()) {
            return;
        }
        Insertmess insertmessage = new Insertmess(this, activity);
        insertmessage.run(message, conversationId, list);
    }

    public void InsertNewMessage(final String message, List<AttachmentInput> list) {
        if (!isNetworkConnected()) {
            return;
        }

        Insertnewmess insertnewmessage = new Insertnewmess(this, activity);
        insertnewmessage.run(message, list);
    }

    public void getConversations() {
        if (!isNetworkConnected()) {
            return;
        }
        Getconv getconversation = new Getconv(this, activity);
        getconversation.run();


    }

    public void getMessages(String conversationid) {
        if (!isNetworkConnected()) {
            return;
        }
        Getmess getmess = new Getmess(this, activity);
        getmess.run(conversationid);

    }

    public void getSupporters() {
        if (!isNetworkConnected()) {
            return;
        }
        GetSup getSup = new GetSup(this, activity);
        getSup.run();
    }

    public void getFAQ() {
        if (!isNetworkConnected()) {
            return;
        }
        GetKnowledge getSup = new GetKnowledge(this, activity);
        getSup.run();
    }

    public void getLead() {
        if (!isNetworkConnected()) {
            return;
        }
        GetLead getLead = new GetLead(this, activity);
        getLead.run();
    }

    public void sendLead() {
        if (!isNetworkConnected()) {
            return;
        }
        SendLead sendLead = new SendLead(this, activity);
        sendLead.run();
    }

    public void add(ErxesObserver e) {
        if (observers == null)
            observers = new ArrayList<>();
        observers.clear();
        observers.add(e);
    }

    //    public void isMessengerOnline(){
//        if(!isNetworkConnected()){
//            return;
//        }
//
//        apolloClient.query(IsMessengerOnlineQuery.builder().integrationId(config.integrationId)
//                .build()).enqueue(new ApolloCall.Callback<IsMessengerOnlineQuery.Data>() {
//            @Override
//            public void onResponse(@Nonnull Response<IsMessengerOnlineQuery.Data> response) {
//                if(!response.hasErrors()){
//                    config.isMessengerOnline =  response.data().isMessengerOnline();
//                    notefyAll(ReturnType.IsMessengerOnline,null,null);
//                }
//                else
//                    notefyAll(ReturnType.SERVERERROR,null,null);
//            }
//
//            @Override
//            public void onFailure(@Nonnull ApolloException e) {
//                Log.d(TAG,"IsMessengerOnline failed ");
//                notefyAll(ReturnType.CONNECTIONFAILED,null,null);
//            }
//        });
//    }
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void remove(ErxesObserver e) {
        if (observers == null)
            observers = new ArrayList<>();
        observers.clear();
    }

    public void notefyAll(int returnType, String conversationId, String message) {
        if (observers == null) return;
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).notify(returnType, conversationId, message);
        }
    }
}
