package com.newmedia.erxeslibrary.Configuration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;

//import com.newmedia.erxes.basic.IsMessengerOnlineQuery;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.graphqlfunction.GetInteg;
import com.newmedia.erxeslibrary.graphqlfunction.GetSup;
import com.newmedia.erxeslibrary.graphqlfunction.Getconv;
import com.newmedia.erxeslibrary.graphqlfunction.Getmess;
import com.newmedia.erxeslibrary.graphqlfunction.Insertmess;
import com.newmedia.erxeslibrary.graphqlfunction.Insertnewmess;
import com.newmedia.erxeslibrary.graphqlfunction.SetConnect;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import okhttp3.OkHttpClient;

public class ErxesRequest {
    final private String TAG = "erxesrequest";

    public ApolloClient apolloClient;
    private OkHttpClient okHttpClient;
    private Context context;
    private List<ErxesObserver> observers;
    private Config config;

    static public ErxesRequest erxesRequest;
    static public ErxesRequest getInstance(Config config){
        if(erxesRequest == null)
            erxesRequest = new ErxesRequest(config);
        return erxesRequest;
    }
    private ErxesRequest(Config config){
        this.context = config.context;
        this.config = config;
        Realm.init(context);
        Helper.Init(context);
    }
    public void set_client(){
        if(config.HOST_3100!=null)
        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(config.HOST_3100)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(config.HOST_3300, okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON,new JsonCustomTypeAdapter())
//                .addCustomTypeAdapter(com.newmedia.erxes.subscription.type.CustomType.JSON,new JsonCustomTypeAdapter())
                .build();
    }

    public void setConnect(String email ,String phone){
        if(!isNetworkConnected()){
            return;
        }
        SetConnect setConnect = new SetConnect(this,context);
        setConnect.run(email,phone);
    }

    public void getIntegration(){
        if(!isNetworkConnected()){
            return;
        }
        GetInteg getIntegration = new GetInteg(this,context);
        getIntegration.run();
    }

    public void InsertMessage( String message, String conversationId,List<JSONObject> list){
        if(!isNetworkConnected()){
            return;
        }
        Insertmess insertmessage = new Insertmess(this,context);
        insertmessage.run(message,conversationId,list);
    }
    public void InsertNewMessage(final String message,List<JSONObject> list){
        if(!isNetworkConnected()){
            return;
        }

        Insertnewmess insertnewmessage = new Insertnewmess(this,context);
        insertnewmessage.run(message,list);
    }

    public void getConversations(){
        if(!isNetworkConnected()){
            return;
        }
        Getconv getconversation = new Getconv(this,context);
        getconversation.run();


    }
    public void getMessages( String conversationid){
        if(!isNetworkConnected()){
            return;
        }
        Getmess getmess = new Getmess(this,context);
        getmess.run(conversationid);

    }
    public void getSupporters( ){
        if(!isNetworkConnected()){
            return;
        }
        GetSup getSup = new GetSup(this,context);
        getSup.run();
    }

    public void add(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
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
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public void remove(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
        observers.clear();
    }

    public void notefyAll( int returnType,String conversationId, String message){
        if(observers == null) return;
        for( int i = 0; i < observers.size(); i++ ){
            observers.get(i).notify(returnType,conversationId,message);
        }
    }
}
