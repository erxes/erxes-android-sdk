package com.newmedia.erxeslibrary.Configuration;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
//import android.support.v7.app.NotificationCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxes.subscription.ConversationsChangedSubscription;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ErxesActivity;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.R;


import java.lang.ref.PhantomReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

public class ListenerService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static OkHttpClient okHttpClient;
    static ApolloClient apolloClient;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("erxesservice","oncreat??");
//        startListen();
        DataManager dataManager;
        dataManager = new DataManager(this);

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
            Realm.init(this);
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Conversation> list=
                    realm.where(Conversation.class).equalTo("status","open").findAll();
            for(int i = 0; i< list.size();i++) {
                Log.d("erxesservice","--"+list.get(i).get_id());
                conversation_listen(list.get(i).get_id());
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
                                        Log.e("erxesservice","runnable" );

                                        conversation_listen(conversationId);


                                    }
                                }).start();
                                e.printStackTrace();
                            }

                            @Override public void onNext(Response<ConversationMessageInsertedSubscription.Data> response) {
                                if(!response.hasErrors()){

                                    if(!ErxesRequest.ConversationMessageSubsribe_handmade(response.data().conversationMessageInserted()))
                                    {
                                        String chat_message = response.data().conversationMessageInserted().content();
                                        String name = response.data().conversationMessageInserted().user().details().fullName();

                                        createNotificationChannel(chat_message,name,response.data().conversationMessageInserted().conversationId());
                                    }
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

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "123")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(name)
                .setContentText( chat_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, mBuilder.build());
    }
    public void startListen(){

        if (!Config.isNetworkConnected()){
//            Log.d("erxesservice","haven't network");
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                   try {
//                       Thread.sleep(5000);
//                       Log.d("erxesservice"," 5000");
//                   }
//                   catch (InterruptedException e){
//                       e.printStackTrace();
//                   }
//                }
//            }).start();
            return;

        }
        Log.d("erxesservice"," listen");



        ApolloSubscriptionCall<ConversationsChangedSubscription.Data> subscriptionCall1 = apolloClient
                .subscribe(ConversationsChangedSubscription.builder().customerId(Config.customerId).build());
        disposables.add(Rx2Apollo.from(subscriptionCall1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(
                        new DisposableSubscriber<Response<ConversationsChangedSubscription.Data>>() {

                            @Override
                            protected void onStart() {
                                super.onStart();
                                Log.d("erxesservice","onstart2");
                            }

                            @Override public void onError(Throwable e) {
                                Log.d("erxesservice","onerror2");
                                e.printStackTrace();
                            }

                            @Override public void onNext(Response<ConversationsChangedSubscription.Data> response) {
                                if(!response.hasErrors()){
                                    Log.d("erxesserive","2::"+response.data().conversationsChanged().type()+" "+response.data().conversationsChanged().customerId());
//                                    Api3100.ConversationMessageSubsribe_handmade(response.data().conversationMessageInserted());

                                }
                                Log.d("erxesservice","onnext2");


                            }

                            @Override public void onComplete() {
                                Log.d("erxesservice","oncomplete2");
                            }
                        }
                )
        );
    }


}
