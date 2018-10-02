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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private OkHttpClient okHttpClient;
    private ApolloClient apolloClient;
    private RealmConfiguration myConfig;
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        myConfig = Helper.getRealmConfig();

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


    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private   CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String id = null;
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                id = bundle.getString("id", null);
        }
        if(id==null){

            Realm realm = Realm.getInstance(myConfig);
            RealmResults<Conversation> list=
                    realm.where(Conversation.class).equalTo("status","open").findAll();
            for(int i = 0; i< list.size();i++) {
                conversation_listen(list.get(i)._id);
            }
        }else{
            conversation_listen(id);

        }
        Log.d("erxesservice","onstartcommand");
        return super.onStartCommand(intent, flags, startId);


    }

    public void conversation_listen(final String conversationId){
        if(!isNetworkConnected()){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    conversation_listen(conversationId);



                }
            }).start();
            return;
        }
        ApolloSubscriptionCall<ConversationMessageInsertedSubscription.Data> subscriptionCall;
        if(apolloClient==null)
            return;
        subscriptionCall=
                apolloClient
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
                                Log.d("erxesservice","onstart"+conversationId);
                            }

                            @Override public void onError(Throwable e) {
                                Log.d("erxesservice","onerror");
                                disposables.delete(this);
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e1) {
                                            e1.printStackTrace();
                                        }
                                        Log.d("erxesservice","runnable" );

                                        conversation_listen(conversationId);


                                    }
                                }).start();
                                e.printStackTrace();
                            }

                            @Override public void onNext(Response<ConversationMessageInsertedSubscription.Data> response) {
                                if(!response.hasErrors()){
                                    if(ErxesRequest.erxesRequest != null) {
                                        ErxesRequest.erxesRequest.ConversationMessageSubsribe_handmade(response.data().conversationMessageInserted());
                                        Log.d("erxesservice","alive");
                                    }
                                    if(ConversationListActivity.chat_is_going==false) {
                                        Log.d("erxesservice","dead");
                                        String chat_message = response.data().conversationMessageInserted().content();
                                        String name = response.data().conversationMessageInserted().user().details().fullName();

                                        createNotificationChannel(chat_message,name,response.data().conversationMessageInserted().conversationId());
                                        Realm inner = Realm.getInstance(myConfig);
                                        inner.beginTransaction();
                                        inner.insertOrUpdate(ConversationMessage.convert(response.data().conversationMessageInserted()));
                                        inner.commitTransaction();


                                        Conversation conversation = inner.where(Conversation.class).equalTo("_id",response.data().conversationMessageInserted().conversationId()).findFirst();

                                        if(conversation!=null) {
                                            inner.beginTransaction();
                                            conversation.content = (response.data().conversationMessageInserted().content());
                                            conversation.isread = false;
                                            inner.insertOrUpdate(conversation);
                                            inner.commitTransaction();

                                        }
                                        inner.close();
                                    }
//
                                }
                                Log.d("erxesservice","onnext"+conversationId);


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
