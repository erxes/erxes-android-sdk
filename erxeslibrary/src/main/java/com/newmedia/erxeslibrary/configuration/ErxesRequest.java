package com.newmedia.erxeslibrary.configuration;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import com.erxes.io.opens.type.AttachmentInput;
import com.erxes.io.opens.type.CustomType;
import com.newmedia.erxeslibrary.BuildConfig;
import com.newmedia.erxeslibrary.connection.GetGEO;
import com.newmedia.erxeslibrary.connection.GetKnowledge;
import com.newmedia.erxeslibrary.connection.helper.Tls12SocketFactory;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.connection.GetIntegration;
import com.newmedia.erxeslibrary.connection.GetLead;
import com.newmedia.erxeslibrary.connection.GetSupporter;
import com.newmedia.erxeslibrary.connection.GetConversation;
import com.newmedia.erxeslibrary.connection.GetMessage;
import com.newmedia.erxeslibrary.connection.InsertMessage;
import com.newmedia.erxeslibrary.connection.SendLead;
import com.newmedia.erxeslibrary.connection.SetConnect;
import com.newmedia.erxeslibrary.connection.helper.JsonCustomTypeAdapter;
import com.newmedia.erxeslibrary.helper.ErxesHelper;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public final class ErxesRequest {
    public ApolloClient apolloClient;
    private OkHttpClient.Builder okHttpClientBuilder;
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

    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

    void set_client() {
        if (config.host3100 != null) {
            OkHttpClient okHttpClient = getHttpClient();

            apolloClient = ApolloClient.builder()
                    .serverUrl(config.host3100)
                    .okHttpClient(okHttpClient)
                    .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.host3300, okHttpClient))
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                    .build();
        }
    }

    private OkHttpClient getHttpClient() {
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
//                .addInterceptor(logging)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS);

        return enableTls12(client).build();
    }

    private OkHttpClient.Builder enableTls12(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception exc) {
                Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
            }
        }

        return client;
    }

    public void setConnect(boolean isCheckRequired, boolean isUser, boolean hasData, String email, String phone, String data) {
        if (!config.isNetworkConnected()) {
            return;
        }
        SetConnect setConnect = new SetConnect(this, context);
        setConnect.run(isCheckRequired, isUser, hasData, email, phone, data);
    }

    public void getGEO() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetGEO getGEO = new GetGEO(context);
        getGEO.run();
    }

    void getIntegration() {
        if (!config.isNetworkConnected()) {
            return;
        }
        GetIntegration getIntegration = new GetIntegration(this, context);
        getIntegration.run();
    }

    public void InsertMessage(String message, String conversationId, List<AttachmentInput> list, String type) {
        if (!config.isNetworkConnected()) {
            return;
        }
        InsertMessage insertmessage = new InsertMessage(this, context);
        insertmessage.run(message, conversationId, list, type);
    }

    public void getConversations() {
        if (!config.isNetworkConnected())
            return;
        GetConversation getconversation = new GetConversation(this, context);
        getconversation.run();
    }

    public void getMessages(String conversationid) {
        if (!config.isNetworkConnected())
            return;
        GetMessage getMessage = new GetMessage(this, context);
        getMessage.run(conversationid);

    }

    public void getSupporters() {
        if (!config.isNetworkConnected())
            return;
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
