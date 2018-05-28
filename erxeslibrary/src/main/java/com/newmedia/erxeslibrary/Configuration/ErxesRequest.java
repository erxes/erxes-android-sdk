package com.newmedia.erxeslibrary.Configuration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.IsMessengerOnlineQuery;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.Model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import io.realm.Realm;
import okhttp3.OkHttpClient;

public class ErxesRequest {
    static public ApolloClient apolloClient;
    static private OkHttpClient okHttpClient;
    final static private String TAG="erxesrequest";
    static private DataManager dataManager;
    static private Realm realm ;
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


        dataManager = new DataManager(context);
        ErxesRequest.context=context;
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }
    static public void changeLanguage(String lang) {
        Locale myLocale;
        if (lang.equalsIgnoreCase(""))
            return;
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
        apolloClient.mutate(MessengerConnectMutation.builder().brandCode(Config.brandCode).email(email).phone(phone).build()).enqueue(new ApolloCall.Callback<MessengerConnectMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<MessengerConnectMutation.Data> response) {
                if(!response.hasErrors()) {

                    Config.customerId = response.data().messengerConnect().customerId();
                    Config.integrationId = response.data().messengerConnect().integrationId();
                    Config.language = response.data().messengerConnect().languageCode();

                    dataManager.setData(DataManager.customerId, Config.customerId);
                    dataManager.setData(DataManager.integrationId, Config.integrationId);
                    dataManager.setData(DataManager.language, Config.language);
                    Log.d(TAG,"init data "+Config.integrationId+" "+Config.customerId+" "+Config.language);
                    if(Config.language!=null)
                        changeLanguage(Config.language);
                    try {

                        if(response.data().messengerConnect().uiOptions()!=null) {
                            JSONObject js = new JSONObject(response.data().messengerConnect().uiOptions().toString());
                            String color = js.getString("color");
                            dataManager.setData(DataManager.color, color);

                            Config.color = color;

                            color = js.getString("wallpaper");
                            dataManager.setData("wallpaper", color);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if(response.data().messengerConnect().messengerData()!=null) {
                            JSONObject js = new JSONObject(response.data().messengerConnect().messengerData().toString());
                            String temp = js.getString("thankYouMessage");
                            dataManager.setData("thankYouMessage", temp);
                            Config.thankYouMessage = temp;
                            temp = js.getString("awayMessage");
                            dataManager.setData("awayMessage", temp);
                            Config.awayMessage = temp;
                            temp = js.getString("welcomeMessage");
                            dataManager.setData("welcomeMessage", temp);
                            Config.welcomeMessage = temp;
                            temp = js.getString("timezone");
                            dataManager.setData("timezone", temp);
                            Config.timezone = temp;

                            temp = js.getString("availabilityMethod");
                            dataManager.setData("availabilityMethod", temp);
                            Config.availabilityMethod = temp;

                            boolean bool = js.getBoolean("isOnline");
                            dataManager.setData("isOnline", bool);
                            Config.isMessengerOnline = bool;
                            bool = js.getBoolean("notifyCustomer");
                            dataManager.setData("notifyCustomer", bool);
                            Config.notifyCustomer = bool;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    notefyAll(true,null);
                }
                else{
                    Log.d(TAG, "errors " + response.errors().toString());
                    notefyAll(false,null);
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                notefyAll(false,null);
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
                message(message).
                attachments(list).
                conversationId(conversationId);
//        if(list.size() > 0 )
//        {
//            temp.attachments(list);
//        }

        apolloClient.mutate(temp
                .build()).enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {
                if(response.hasErrors())
                    Log.d(TAG, "errors " + response.errors().toString());
                else {

                    ConversationMessage a = new ConversationMessage();
                    a.setConversationId(response.data().insertMessage().conversationId());
                    a.setCreatedAt(response.data().insertMessage().createdAt());
                    a.set_id( response.data().insertMessage()._id());
                    a.setContent(message);
                    if(response.data().insertMessage().attachments()!=null)
                    a.setAttachments(response.data().insertMessage().attachments().toString());
                    a.setInternal(false);
                    a.setCustomerId(Config.customerId);



                    Realm inner = Realm.getDefaultInstance();
                    inner.beginTransaction();
                    inner.insertOrUpdate(a);
                    inner.commitTransaction();
                    inner.close();
                    notefyAll(true,conversationId);
                }
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
                e.printStackTrace();
                Log.d(TAG, "failed ");
            }
        });
    }
    static public void InsertNewMessage(final String message,List<JSONObject> list){
        if(!isNetworkConnected()){
            return;
        }
        apolloClient.mutate(InsertMessageMutation.builder().integrationId(Config.integrationId).
                customerId(Config.customerId).message(message).conversationId("").attachments(list).build()).enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<InsertMessageMutation.Data> response) {
                if(response.hasErrors()) {
                    Log.d(TAG, "errors " + response.errors().toString());
                    notefyAll(false,null);
                }
                else {
                    Log.d(TAG, "cid " + response.data().insertMessage().conversationId());

                    Config.conversationId = response.data().insertMessage().conversationId();
                    Conversation conversation = new Conversation();
                    conversation.set_id(Config.conversationId);
                    conversation.setContent(message);
                    conversation.setStatus("open");
                    conversation.setDate(response.data().insertMessage().createdAt());
                    conversation.setCustomerId(Config.customerId);
                    conversation.setIntegrationId(Config.integrationId);


                    ConversationMessage a = new ConversationMessage();
                    a.setConversationId(Config.conversationId);
                    a.setCreatedAt(response.data().insertMessage().createdAt());
                    a.set_id( response.data().insertMessage()._id());
                    a.setContent(message);
                    a.setInternal(false);
                    a.setCustomerId(Config.customerId);
                    if(response.data().insertMessage().attachments()!=null)
                        a.setAttachments(response.data().insertMessage().attachments().toString());


                    Realm inner = Realm.getDefaultInstance();
                    inner.beginTransaction();
                    inner.insertOrUpdate(a);
                    inner.insertOrUpdate(conversation);
                    inner.commitTransaction();
                    inner.close();

                    ListenerService.conversation_listen(Config.conversationId);

                    notefyAll(true,response.data().insertMessage().conversationId());


                }
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
                e.printStackTrace();
                notefyAll(false,null);
                Log.d(TAG, "failed ");
            }
        });
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
                    notefyAll(true,null);
                    Log.d(TAG,"getconversation ok ");
                }
                else
                    Log.d(TAG,"getconversation 0 ");
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"getconversation failed ");
                e.printStackTrace();
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
                    notefyAll(true,conversationid);

                }

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"getmessages failed ");
            }
        });
    }
    static public void ConversationMessageSubsribe_handmade(ConversationMessageInsertedSubscription.ConversationMessageInserted data){
        ConversationMessage converted = new ConversationMessage();
        converted.set_id(data._id());
        converted.setContent(data.content());
        converted.setConversationId( data.conversationId());
        converted.setCreatedAt( data.createdAt());
        converted.setCustomerId( data.customerId());
        converted.setInternal(data.internal());
        if(data.user() != null) {
            User a = new User();
            a.convert(data.user());
            converted.setUser(a);
        }
        if(data.attachments()!=null)
            converted.setAttachments(data.attachments().toString());

        Realm inner = Realm.getDefaultInstance();
        inner.beginTransaction();
        inner.insertOrUpdate(converted);
        inner.commitTransaction();


        Conversation conversation = inner.where(Conversation.class).equalTo("_id",data.conversationId()).findFirst();

        if(conversation!=null) {
            inner.beginTransaction();
            conversation.setContent(data.content());
            conversation.setIsread(false);



            inner.insertOrUpdate(conversation);
            inner.commitTransaction();
            inner.close();
            notefyAll(true,conversation.get_id());
        }
        else{
            inner.close();
        }

    }
    static public void add(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
        observers.clear();
        observers.add(e);
    }
    static public void isMessengerOnline(String integration){
        if(!isNetworkConnected()){
            return;
        }

        apolloClient.query(IsMessengerOnlineQuery.builder().integrationId(integration)
                .build()).enqueue(new ApolloCall.Callback<IsMessengerOnlineQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<IsMessengerOnlineQuery.Data> response) {
                if(!response.hasErrors()){
                    Config.isMessengerOnline =  response.data().isMessengerOnline();
                }
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.d(TAG,"isMessengerOnline failed ");
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

    private static void notefyAll(boolean status,String conversationId){
        if(observers == null) return;
        for( int i = 0; i < observers.size(); i++ ){
            observers.get(i).notify(status,conversationId);
        }
    }
}
