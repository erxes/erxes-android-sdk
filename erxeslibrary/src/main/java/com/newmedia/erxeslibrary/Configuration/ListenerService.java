package com.newmedia.erxeslibrary.Configuration;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

public class ListenerService extends Service{


    private OkHttpClient okHttpClient;
    private ApolloClient apolloClient;
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

//        startListen();
        DataManager dataManager;
        dataManager =  DataManager.getInstance(this);

        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(dataManager.getDataS("HOST3100"))
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(dataManager.getDataS("HOST3300"), okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON,new JsonCustomTypeAdapter())
                .addCustomTypeAdapter(com.newmedia.erxes.subscription.type.CustomType.JSON,new JsonCustomTypeAdapter())
                .build();


        Log.d("erxesservice","oncreate");
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("erxesservice","destory");
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String id = null;
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                id = bundle.getString("id", null);
        }
        if(id==null){
            DataManager dataManager;
            dataManager =  DataManager.getInstance(this);
            Realm realm = DB.getDB();
            RealmResults<Conversation> list=
                    realm.where(Conversation.class).equalTo("status","open")
                            .equalTo("customerId",dataManager.getDataS(DataManager.customerId))
                            .equalTo("integrationId",dataManager.getDataS(DataManager.integrationId)).findAll();
            for(int i = 0; i< list.size();i++) {
                conversation_listen(list.get(i)._id);
            }
            Log.d("erxesservice","start "+list.size());
        }else{
            conversation_listen(id);
            Log.d("erxesservice","start only one");
        }

        return super.onStartCommand(intent, flags, startId);


    }
    private boolean run_thread(final String conversationId){
        if(!isNetworkConnected()){
            //internetgui yud gesen ug
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Log.d("erxesservice","fucked up");
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    conversation_listen(conversationId);
                }
            }).start();
            return true;
        }
        return false;
    }
    public void conversation_listen(final String conversationId){
        if(run_thread(conversationId))
            return;
        ApolloSubscriptionCall<ConversationMessageInsertedSubscription.Data> subscriptionCall;
        if(apolloClient == null)
            return;
        subscriptionCall = apolloClient
                                .subscribe(ConversationMessageInsertedSubscription.builder()
                                ._id(conversationId)
                                .build());

        disposables.add(Rx2Apollo.from(subscriptionCall)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<ConversationMessageInsertedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                                Log.d("erxesservice","onstarted "+conversationId);
                            }

                            @Override public void onError(Throwable e) {
                                Log.d("erxesservice","onerror");
                                e.printStackTrace();
                                disposables.delete(this);
                                run_thread(conversationId);
                            }

                            @Override public void onNext(Response<ConversationMessageInsertedSubscription.Data> response) {
                                if(!response.hasErrors()){
//                                    if(ErxesRequest.erxesRequest != null) {
//                                        ErxesRequest.erxesRequest.ConversationMessageSubsribe_handmade(response.data().conversationMessageInserted());
//                                        Log.d("erxesservice","alive");
//                                    }
                                    if(ConversationListActivity.chat_is_going==false) {
                                        Log.d("erxesservice","dead");
                                        String chat_message = response.data().conversationMessageInserted().content();
                                        String name = response.data().conversationMessageInserted().user().details().fullName();

                                        createNotificationChannel(chat_message,name,response.data().conversationMessageInserted().conversationId());

                                    }

                                    Realm inner =  DB.getDB();

                                    inner.beginTransaction();
                                    inner.insertOrUpdate(ConversationMessage.convert(response.data().conversationMessageInserted()));
                                    inner.commitTransaction();

                                    Conversation conversation = inner.where(Conversation.class).equalTo("_id",response.data().conversationMessageInserted().conversationId()).findFirst();

                                    Log.d("erxesservice","insert to database");

                                    if(conversation != null) {
                                        inner.beginTransaction();
                                        conversation.content = (response.data().conversationMessageInserted().content());
                                        conversation.isread = false;
                                        inner.insertOrUpdate(conversation);
                                        inner.commitTransaction();
                                    }
                                    inner.close();
//
                                }
                                Log.d("erxesservice","onnext "+conversationId);


                            }

                            @Override public void onComplete() {
                                Log.d("erxesservice","oncomplete");
                            }
                        }
                )
        );
    }
    private void createNotificationChannel(String chat_message,String name,String conversion_id) {


        Intent intent = new Intent(this, ErxesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "erxeschannel")
//                .setBadgeIconType(R.drawable.icon)
//                .setContentTitle(name)
//                .setContentText(Html.fromHtml( chat_message))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(0, mBuilder.build());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(name)
                .setContentText(Html.fromHtml( chat_message))
                .setSmallIcon(R.drawable.icon)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent).getNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, notification);
        Log.d("erxes","notification can");
    }



}
