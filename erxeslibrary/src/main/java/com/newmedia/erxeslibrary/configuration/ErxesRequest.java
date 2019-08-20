package com.newmedia.erxeslibrary.configuration;

import android.content.Context;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import com.newmedia.erxes.basic.type.AttachmentInput;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxeslibrary.graphqlfunction.GetGEO;
import com.newmedia.erxeslibrary.graphqlfunction.GetKnowledge;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.graphqlfunction.GetIntegration;
import com.newmedia.erxeslibrary.graphqlfunction.GetLead;
import com.newmedia.erxeslibrary.graphqlfunction.GetSupporter;
import com.newmedia.erxeslibrary.graphqlfunction.GetConversation;
import com.newmedia.erxeslibrary.graphqlfunction.GetMessage;
import com.newmedia.erxeslibrary.graphqlfunction.InsertMessage;
import com.newmedia.erxeslibrary.graphqlfunction.InsertNewMessage;
import com.newmedia.erxeslibrary.graphqlfunction.SendLead;
import com.newmedia.erxeslibrary.graphqlfunction.SetConnect;
import com.newmedia.erxeslibrary.helper.JsonCustomTypeAdapter2;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public final class ErxesRequest {
    public ApolloClient apolloClient;
    private Context context;
    private List<ErxesObserver> observers;
    private Config config;

    private static ErxesRequest erxesRequest;

    static public ErxesRequest getInstance(Config config) {
        if (erxesRequest == null)
            erxesRequest = new ErxesRequest(config);
        return erxesRequest;
    }

    private ErxesRequest(Config config) {
        this.context = config.context;
        this.config = config;
        ErxesHelper.Init(context);
    }

    void set_client() {
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
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

        ApolloSqlHelper apolloSqlHelper = ApolloSqlHelper.create(context, "db_cache");

        // Create NormalizedCacheFactory
        NormalizedCacheFactory cacheFactory = new SqlNormalizedCacheFactory(apolloSqlHelper);

        // Create the cache key resolver, this example works well when all types have globally unique ids.
        CacheKeyResolver resolver = new CacheKeyResolver() {
            @NotNull
            @Override
            public CacheKey fromFieldRecordSet(@NotNull ResponseField field, @NotNull Map<String, Object> recordSet) {
                return formatCacheKey((String) recordSet.get("id"));
            }

            @NotNull
            @Override
            public CacheKey fromFieldArguments(@NotNull ResponseField field, @NotNull Operation.Variables variables) {
                return formatCacheKey((String) field.resolveArgument("id", variables));
            }

            private CacheKey formatCacheKey(String id) {
                if (id == null || id.isEmpty()) {
                    return CacheKey.NO_KEY;
                } else {
                    return CacheKey.from(id);
                }
            }
        };

        if (config.host3100 != null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new AddCookiesInterceptor(this.context))
                    .addInterceptor(new ReceivedCookiesInterceptor(this.context))
                    .build();
            apolloClient = ApolloClient.builder()
                    .serverUrl(config.host3100)
                    .okHttpClient(okHttpClient)
                    .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.host3300, okHttpClient))
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter2())
                    .build();
        }
    }

    public void setConnect(String email, String phone, boolean isUser, String data) {
        if (!config.isNetworkConnected()) {
            return;
        }
        SetConnect setConnect = new SetConnect(this, context);
        setConnect.run(email, phone, isUser, data);
    }

    public void getGEO() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetGEO getGEO = new GetGEO(context);
        getGEO.run();
    }

    void getIntegration(boolean hasData, String email, String phone, JSONObject jsonObject) {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetIntegration getIntegration = new GetIntegration(this, context);
        getIntegration.run(hasData,email,phone,jsonObject);
    }

    public void InsertMessage(String message, String conversationId, List<AttachmentInput> list) {
        if (!config.isNetworkConnected()) {
            return;
        }
        InsertMessage insertmessage = new InsertMessage(this, context);
        insertmessage.run(message, conversationId, list);
    }

    public void InsertNewMessage(final String message, List<AttachmentInput> list) {
        if (!config.isNetworkConnected()) {
            return;
        }

        InsertNewMessage insertnewmessage = new InsertNewMessage(this, context);
        insertnewmessage.run(message, list);
    }

    public void getConversations() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetConversation getconversation = new GetConversation(this, context);
        getconversation.run();


    }

    public void getMessages(String conversationid) {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetMessage getMessage = new GetMessage(this, context);
        getMessage.run(conversationid);

    }

    public void getSupporters() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetSupporter getSupporter = new GetSupporter(this, context);
        getSupporter.run();
    }

    public void getFAQ() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetKnowledge getSup = new GetKnowledge(this, context);
        getSup.run();
    }

    public void getLead() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetLead getLead = new GetLead(this, context);
        getLead.run();
    }

    public void sendLead() {
        if (!config.isNetworkConnected()) {
            return;
        }
        SendLead sendLead = new SendLead(this, context);
        sendLead.run();
    }

    public void add(ErxesObserver e) {
        if (observers == null)
            observers = new ArrayList<>();
        observers.clear();
        observers.add(e);
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
