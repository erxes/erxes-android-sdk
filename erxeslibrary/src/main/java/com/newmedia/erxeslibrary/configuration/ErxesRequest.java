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
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ErxesRequest {
    final private String TAG = "erxesrequest";

    public ApolloClient apolloClient;
    private OkHttpClient.Builder okHttpClientBuilder;
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

    public void set_client() throws GeneralSecurityException {
        if (config.HOST_3100 != null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClientBuilder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .cache(null)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new AddCookiesInterceptor(this.activity))
                    .addInterceptor(new ReceivedCookiesInterceptor(this.activity));
//            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
//                try {
//                    final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                            .cipherSuites(customCipherSuites.toArray(new CipherSuite[0]))
//                            .build();
//                    X509TrustManager trustManager = defaultTrustManager();
//                    SSLSocketFactory sslSocketFactory = defaultSslSocketFactory(trustManager);
//                    SSLSocketFactory customSslSocketFactory = new DelegatingSSLSocketFactory(sslSocketFactory) {
//                        @Override
//                        protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
//                            socket.setEnabledCipherSuites(javaNames(spec.cipherSuites()));
//                            return socket;
//                        }
//                    };
//                    okHttpClientBuilder.sslSocketFactory(customSslSocketFactory,trustManager);
//
//                    } catch (Exception exc) {
//                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
//                }
//            }
//            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
//                try {
//                    SSLContext sc = SSLContext.getInstance("TLSv1.2");
//                    sc.init(null, null, null);
//                    okHttpClientBuilder.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
//
//                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
//                            .tlsVersions(TlsVersion.TLS_1_2)
//                            .build();
//
//                    List<ConnectionSpec> specs = new ArrayList<>();
//                    specs.add(cs);
//                    specs.add(ConnectionSpec.COMPATIBLE_TLS);
//                    specs.add(ConnectionSpec.CLEARTEXT);
//
//                    okHttpClientBuilder.connectionSpecs(specs);
//                } catch (Exception exc) {
//                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
//                }
//            }
            apolloClient = ApolloClient.builder()
                    .serverUrl(config.HOST_3100)
                    .okHttpClient(okHttpClientBuilder.build())
                    .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.HOST_3300, okHttpClientBuilder.build()))
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                    .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter2())
                    .build();
        }
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

    /**
     * Returns a trust manager that trusts the VM's default certificate authorities.
     */
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

    /**
     * An SSL socket factory that forwards all calls to a delegate. Override {@link #configureSocket}
     * to customize a created socket before it is returned.
     */
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

    public void getIntegration(boolean hasData, String email, String phone, JSONObject jsonObject) {
        if (!isNetworkConnected()) {
            return;
        }
        GetInteg getIntegration = new GetInteg(this, activity);
        getIntegration.run(hasData, email, phone, jsonObject);
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
