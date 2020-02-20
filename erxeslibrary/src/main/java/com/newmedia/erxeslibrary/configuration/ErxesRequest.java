package com.newmedia.erxeslibrary.configuration;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.cache.normalized.CacheKey;
import com.apollographql.apollo.cache.normalized.CacheKeyResolver;
import com.apollographql.apollo.cache.normalized.NormalizedCacheFactory;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

import com.erxes.io.opens.type.AttachmentInput;
import com.erxes.io.opens.type.CustomType;
import com.newmedia.erxeslibrary.connection.GetGEO;
import com.newmedia.erxeslibrary.connection.GetKnowledge;
import com.newmedia.erxeslibrary.connection.helper.AddCookiesInterceptor;
import com.newmedia.erxeslibrary.connection.helper.ReceivedCookiesInterceptor;
import com.newmedia.erxeslibrary.connection.helper.Tls12SocketFactory;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.connection.GetIntegration;
import com.newmedia.erxeslibrary.connection.GetLead;
import com.newmedia.erxeslibrary.connection.GetSupporter;
import com.newmedia.erxeslibrary.connection.GetConversation;
import com.newmedia.erxeslibrary.connection.GetMessage;
import com.newmedia.erxeslibrary.connection.InsertMessage;
import com.newmedia.erxeslibrary.connection.InsertNewMessage;
import com.newmedia.erxeslibrary.connection.SendLead;
import com.newmedia.erxeslibrary.connection.SetConnect;
import com.newmedia.erxeslibrary.connection.helper.JsonCustomTypeAdapter;
import com.newmedia.erxeslibrary.helper.ErxesHelper;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
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

    void set_client() {
        ApolloSqlHelper apolloSqlHelper = ApolloSqlHelper.create(context, "db_cache");

        NormalizedCacheFactory cacheFactory = new SqlNormalizedCacheFactory(apolloSqlHelper);

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
            OkHttpClient okHttpClient = getHttpClient();

            apolloClient = ApolloClient.builder()
                    .serverUrl(config.host3100)
//                    .normalizedCache(cacheFactory, resolver)
                    .okHttpClient(okHttpClient)
                    .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.host3300, okHttpClient))
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                    .build();
        }
    }

    private OkHttpClient getHttpClient() {
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .cache(null)
//                .addInterceptor(logging)
                .addInterceptor(new AddCookiesInterceptor(this.context))
                .addInterceptor(new ReceivedCookiesInterceptor(this.context))
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

    private List<CipherSuite> customCipherSuites = Arrays.asList(
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256);

    private SSLSocketFactory defaultSslSocketFactory(X509TrustManager trustManager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, null);

        return sslContext.getSocketFactory();
    }

    private X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private String[] javaNames(List<CipherSuite> cipherSuites) {
        String[] result = new String[cipherSuites.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cipherSuites.get(i).javaName();
        }
        return result;
    }

    static class DelegatingSSLSocketFactory extends SSLSocketFactory {
        protected final SSLSocketFactory delegate;

        DelegatingSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(
                Socket socket, String host, int port, boolean autoClose) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(
                String host, int port, InetAddress localHost, int localPort) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(
                InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(
                    address, port, localAddress, localPort));
        }

        private static final String[] TLS_V1_2 = {"TLSv1.2"};

        protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
            if (socket != null) {
                socket.setEnabledProtocols(TLS_V1_2);
            }
            return socket;
        }
    }

    public void setConnect(boolean isFromProvider, boolean isCheckRequired, boolean isUser, boolean hasData, String email, String phone, String data) {
        if (!config.isNetworkConnected()) {
            return;
        }
        SetConnect setConnect = new SetConnect(this, context);
        setConnect.run(isFromProvider, isCheckRequired, isUser, hasData, email, phone, data);
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
        getIntegration.run(hasData, email, phone, jsonObject);
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
