package com.newmedia.erxeslibrary.Configuration;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.GetMessengerIntegrationQuery;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.IsMessengerOnlineQuery;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.ConversationListActivity;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmModel;
import okhttp3.OkHttpClient;

public class ErxesRequest {
    static public ApolloClient apolloClient;
    static private OkHttpClient okHttpClient;
    final static private String TAG="erxesrequest";
    static private DataManager dataManager;
    static private Context context;
    static private List<ErxesObserver> observers;
    static public void init(Context context){
        if(Config.brandCode != null)
            return;


        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(Config.HOST_3100)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(Config.HOST_3300, okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON,new JsonCustomTypeAdapter())
                .build();


        dataManager =  DataManager.getInstance(context);
        ErxesRequest.context=context;
        Realm.init(context);
        Helper.Init(context);


    }
    static public void changeLanguage(String lang) {
        if(lang == null || lang.equalsIgnoreCase("") )
            return;


        Config.language = lang ;
        dataManager.setData(DataManager.language, Config.language);

        Locale myLocale;
        myLocale = new Locale(lang);

        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

    }
    static public void setConnect(String email ,String phone){
        if(!isNetworkConnected()){
            return;
        }
        apolloClient.mutate(MessengerConnectMutation.builder().brandCode(Config.brandCode).email(email).phone(phone).build()).enqueue(
                new ApolloCall.Callback<MessengerConnectMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<MessengerConnectMutation.Data> response) {
                if(!response.hasErrors()) {

                    Config.customerId = response.data().messengerConnect().customerId();
                    Config.integrationId = response.data().messengerConnect().integrationId();

                    dataManager.setData(DataManager.customerId, Config.customerId);
                    dataManager.setData(DataManager.integrationId, Config.integrationId);

                    changeLanguage(response.data().messengerConnect().languageCode());
                    Helper.load_uiOptions(response.data().messengerConnect().uiOptions());
                    Helper.load_messengerData( response.data().messengerConnect().messengerData());


                    notefyAll(ReturnType.LOGIN_SUCCESS,null,null);
                }
                else{

                    Log.d(TAG, "errors " + response.errors().toString());
                    notefyAll(ReturnType.SERVERERROR,null, response.errors().get(0).message());
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                notefyAll(ReturnType.CONNECTIONFAILED,null ,e.getMessage());
                Log.d(TAG, "failed ");
                e.printStackTrace();

            }
        });
    }

    static public void getIntegration(String brandCode){
        if(!isNetworkConnected()){
            return;
        }
        apolloClient.query(GetMessengerIntegrationQuery.builder().brandCode(Config.brandCode).build())
                .enqueue(new ApolloCall.Callback<GetMessengerIntegrationQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetMessengerIntegrationQuery.Data> response) {
                        if(!response.hasErrors()) {


                            changeLanguage(response.data().getMessengerIntegration().languageCode());
                            Helper.load_uiOptions(response.data().getMessengerIntegration().uiOptions());
                            Helper.load_messengerData( response.data().getMessengerIntegration().messengerData());

                            notefyAll(ReturnType.INTEGRATION_CHANGED,null ,null);
                        }
                        else{
                            Log.d(TAG, "errors " + response.errors().toString());
                            notefyAll(ReturnType.SERVERERROR,null,response.errors().get(0).message());
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        notefyAll(ReturnType.CONNECTIONFAILED,null, e.getMessage());
                        Log.d(TAG, "failed ");
                        e.printStackTrace();

                    }
                });
    }

    static public void InsertMessage(final String message, final String conversationId,List<JSONObject> list){
        if(!isNetworkConnected()){
            return;
        }
        InsertMessageMutation.Builder temp =InsertMessageMutation.builder().
                integrationId(Config.integrationId).
                customerId(Config.customerId).
                message(message).attachments(list).
                conversationId(conversationId);


        apolloClient.mutate(temp.build()).enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {
                if(response.hasErrors()) {
                    Log.d(TAG, "errors " + response.errors().toString());
                    notefyAll(ReturnType.SERVERERROR,conversationId,response.errors().get(0).message());
                }
                else {
                    ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),message);
                    async_update_database(a);
                    notefyAll(ReturnType.Mutation,conversationId,null);
                }
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
                e.printStackTrace();
                notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
                Log.d(TAG, "failed ");
            }
        });
    }
    static public void InsertNewMessage(final String message,List<JSONObject> list){
        if(!isNetworkConnected()){
            return;
        }
        apolloClient.mutate(InsertMessageMutation.builder()
                .integrationId(Config.integrationId)
                .customerId(Config.customerId)
                .message(message)
                .conversationId("")
                .attachments(list).build())
                .enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {
                if(response.hasErrors()) {
                    Log.d(TAG, "errors " + response.errors().toString());
                    notefyAll(ReturnType.SERVERERROR,null,response.errors().get(0).message());
                }
                else {
                    Log.d(TAG, "cid " + response.data().insertMessage().conversationId());

                    Conversation conversation = Conversation.update(response.data().insertMessage(),message);
                    ConversationMessage a = ConversationMessage.convert(response.data().insertMessage(),message);
                    async_update_database(conversation);
                    async_update_database(a);
                    Intent intent2 = new Intent(context, ListenerService.class);
                    intent2.putExtra("id",Config.conversationId);
                    context.startService(intent2);
//                    ListenerService.conversation_listen(Config.conversationId);

                    notefyAll(ReturnType.Mutation_new,response.data().insertMessage().conversationId(),null);


                }
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
                e.printStackTrace();
                notefyAll( ReturnType.CONNECTIONFAILED,null,e.getMessage());
                Log.d(TAG, "failed ");
            }
        });
    }
    private static void async_update_database(RealmModel realmModel){
        Realm inner = Realm.getDefaultInstance();
        inner.beginTransaction();
        inner.insertOrUpdate(realmModel);
        inner.commitTransaction();
        inner.close();
    }
    static public void getConversations(){
        if(!isNetworkConnected()){
            return;
        }

        apolloClient.query(ConversationsQuery.builder().integrationId(Config.integrationId).
                customerId(Config.customerId).build()).enqueue(new ApolloCall.Callback<ConversationsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ConversationsQuery.Data> response) {

                if(response.data().conversations().size()>0) {
                    final List<Conversation> data = Conversation.convert(response);

                    Realm inner = Realm.getDefaultInstance();
                    inner.beginTransaction();
                    inner.delete(Conversation.class);
                    inner.copyToRealm(data);
                    inner.commitTransaction();
                    inner.close();

                    Log.d(TAG,"getconversation ok ");
                }
                else
                    Log.d(TAG,"getconversation 0 ");
                notefyAll(ReturnType.getconversation,null,null);
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"getconversation failed ");
                e.printStackTrace();
                notefyAll(ReturnType.CONNECTIONFAILED,null,e.getMessage());
            }
        });
    }
    static public void getMessages(final String conversationid){
        if(!isNetworkConnected()){
            return;
        }
        apolloClient.query(MessagesQuery.builder().conversationId(conversationid)
                .build()).enqueue(new ApolloCall.Callback<MessagesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<MessagesQuery.Data> response) {

                if(response.data().messages().size() > 0) {
                    List<ConversationMessage> data = ConversationMessage.convert(response,conversationid);


                    Realm inner = Realm.getDefaultInstance();
                    inner.beginTransaction();
                    inner.copyToRealmOrUpdate(data);
                    inner.commitTransaction();
                    inner.close();
                }
                notefyAll(ReturnType.getmessages,conversationid,null);

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"getmessages failed ");
            }
        });
    }
    static public boolean ConversationMessageSubsribe_handmade(ConversationMessageInsertedSubscription.ConversationMessageInserted data){

      ;

        Realm inner = Realm.getDefaultInstance();
        inner.beginTransaction();
        inner.insertOrUpdate(ConversationMessage.convert(data));
        inner.commitTransaction();


        Conversation conversation = inner.where(Conversation.class).equalTo("_id",data.conversationId()).findFirst();

        if(conversation!=null) {
            inner.beginTransaction();
            conversation.content = (data.content());
            conversation.isread = false;
            inner.insertOrUpdate(conversation);
            inner.commitTransaction();
            inner.close();
            notefyAll(ReturnType.subscription,conversation._id,null);
        }
        else{
            inner.close();
        }
        return ConversationListActivity.chat_is_going;

    }
    static public void add(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
        observers.clear();
        observers.add(e);
    }
    static public void isMessengerOnline(){
        if(!isNetworkConnected()){
            return;
        }

        apolloClient.query(IsMessengerOnlineQuery.builder().integrationId(Config.integrationId)
                .build()).enqueue(new ApolloCall.Callback<IsMessengerOnlineQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<IsMessengerOnlineQuery.Data> response) {
                if(!response.hasErrors()){
                    Config.isMessengerOnline =  response.data().isMessengerOnline();
                    notefyAll(ReturnType.isMessengerOnline,null,null);
                }
                else
                    notefyAll(ReturnType.SERVERERROR,null,null);
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"isMessengerOnline failed ");
                notefyAll(ReturnType.CONNECTIONFAILED,null,null);
            }
        });
    }
    static public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    static public void remove(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
        observers.clear();
    }

    private static void notefyAll( ReturnType returnType,String conversationId, String message){
        if(observers == null) return;
        for( int i = 0; i < observers.size(); i++ ){
            observers.get(i).notify(returnType,conversationId,message);
        }
    }
}
