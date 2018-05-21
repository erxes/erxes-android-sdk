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
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.basic.MessengerConnectMutation;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;
import okhttp3.OkHttpClient;

public class Config {
//    final static public String HOST="192.168.1.6";
    final static private String HOST="192.168.86.39";
    final static public String HOST_3100="http://"+HOST+":3100/graphql";
    final static public String HOST_3300="ws://"+HOST+":3300/subscriptions";
    static public String customerId;
    static public String integrationId;
    static public String color,wallpaper;
    static private String language;
    static private String thankYouMessage;
    static private String awayMessage;
    static public String welcomeMessage;
    static private String timezone;
    static private String availabilityMethod;

    static public String conversationId=null; ///public
    static private String brandCode;
    static private boolean isOnline,notifyCustomer;


    static private ApolloClient apolloClient;
    static private OkHttpClient okHttpClient;
    static private List<ErxesObserver> observers;
    static private DataManager dataManager;
    static private Realm realm ;
    static private Context context;
    static private final String TAG="api3100";
    static public String convert_datetime(Long createDate) {
        Long diffTime = Calendar.getInstance().getTimeInMillis()  - createDate;

        diffTime = diffTime/1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;

        if(weeks > 0){
            return ( ""+weeks+" 7 хоногийн өмнө");
        }
        else if(days>0){
            return ( ""+days+" өдрийн өмнө");
        }
        else if(hours>0){
            return ( ""+hours+" цагийн өмнө");
        }
        else if(minutes>0){
            return ( ""+minutes+" минутын өмнө");
        }
        else {
            return ( ""+seconds+" секундын өмнө");
        }
    }
    static public String Message_datetime(String createDate_s) {

        Long createDate = null;
        try {
            createDate =Long.valueOf(createDate_s);
        }
        catch (NumberFormatException e){
            return "";
        }


        Date date = new Date();
        date.setTime(createDate);

        Long diffTime = Calendar.getInstance().getTimeInMillis()  - createDate;

        diffTime = diffTime/1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        SimpleDateFormat format =
                new SimpleDateFormat("HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("EEE HH:mm");
        SimpleDateFormat format3 =
                new SimpleDateFormat("MMM d,HH:mm");
//        new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        if(weeks > 0){
            return format3.format(date);
        }
        else if(days>0){
            return format2.format(date);
        }
        else {
            return format.format(date);
        }

    }

    static public void LoggedInDefault(){
        Config.customerId = dataManager.getDataS(DataManager.customerId);
        Config.integrationId = dataManager.getDataS(DataManager.integrationId);
        Config.welcomeMessage = dataManager.getDataS("welcomeMessage");
        Config.color= dataManager.getDataS(DataManager.color);
        Config.wallpaper= dataManager.getDataS("wallpaper");
    }
    static public boolean isLoggedIn(){
        if(dataManager.getDataS(DataManager.customerId)==null)
            return false;
        return true;
    }
    static public boolean Logout(){
        Config.customerId = null;
        dataManager.setData(DataManager.customerId,null);
        dataManager.setData(DataManager.integrationId,null);
        return true;
    }
    static public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public static ApolloClient getApolloClient() {
        return apolloClient;
    }


    static public void add(ErxesObserver e){
        if(observers == null)
            observers= new ArrayList<>();
        observers.clear();
        observers.add(e);
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
    static public void init(Context context,String brandcode){
        if(Config.brandCode != null)
            return;
        Config.brandCode = brandcode;

        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(Config.HOST_3100)
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(Config.HOST_3300, okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON,new JsonCustomTypeAdapter())
                .build();


        dataManager = new DataManager(context);
        Config.context=context;
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }
    static public void setConnect(String email ,String phone){
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
                            Config.isOnline = bool;
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
    static public void InsertMessage(final String message, final String conversationId){
        String integrationId = dataManager.getDataS(DataManager.integrationId);
        String customerId = dataManager.getDataS(DataManager.customerId);
        apolloClient.mutate(InsertMessageMutation.builder().integrationId(integrationId).
                customerId(customerId).message(message).conversationId(conversationId).build()).enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
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
    static public void InsertNewMessage(final String message){

        apolloClient.mutate(InsertMessageMutation.builder().integrationId(Config.integrationId).
                customerId(Config.customerId).message(message).conversationId("").build()).enqueue(new ApolloCall.Callback<InsertMessageMutation.Data>() {
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

        Log.d(TAG,"handmade"+data.isCustomerRead());
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

}
