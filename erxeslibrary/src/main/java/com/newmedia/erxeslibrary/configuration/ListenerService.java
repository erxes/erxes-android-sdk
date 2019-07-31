package com.newmedia.erxeslibrary.configuration;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.newmedia.erxes.basic.type.CustomType;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.OkHttpClient;

public class ListenerService extends Service {

    private static final String TAG = ListenerService.class.getName();
    private OkHttpClient okHttpClient;
    private ApolloClient apolloClient;
    private CompositeDisposable disposables = new CompositeDisposable();
    Config config;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        config = Config.getInstance(this);
//        startListen();
        DataManager dataManager;
        dataManager = DataManager.getInstance(this);

        okHttpClient = new OkHttpClient.Builder().build();
        apolloClient = ApolloClient.builder()
                .serverUrl(dataManager.getDataS("HOST3100"))
                .okHttpClient(okHttpClient)
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(dataManager.getDataS("HOST3300"), okHttpClient))
                .addCustomTypeAdapter(CustomType.JSON, new JsonCustomTypeAdapter())
                .addCustomTypeAdapter(com.newmedia.erxes.subscription.type.CustomType.JSON, new JsonCustomTypeAdapter())
                .build();


        Log.e(TAG, "oncreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "destory");
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String id = null;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null)
                id = bundle.getString("id", null);
        }
        if (id == null) {
            DataManager dataManager;
            dataManager = DataManager.getInstance(this);
            Log.d(TAG, "start " + config.conversations.size());
            if (disposables.size() != config.conversations.size()) {
                disposables.clear();
                for (int i = 0; i < config.conversations.size(); i++) {
                    conversation_listen(config.conversations.get(i)._id);
                }
            }
        } else {
            conversation_listen(id);
            Log.e(TAG, "start only one");
        }

        return super.onStartCommand(intent, flags, startId);


    }

    private boolean run_thread(final String conversationId) {
        if (!isNetworkConnected()) {
            //internetgui yud gesen ug
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Log.e(TAG, "subscribe thread running ");
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

    public void conversation_listen(final String conversationId) {
        if (run_thread(conversationId))
            return;
        ApolloSubscriptionCall<ConversationMessageInsertedSubscription.Data> subscriptionCall;
        if (apolloClient == null)
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
                                Log.e(TAG, "onstarted " + conversationId);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e(TAG, "onerror " + conversationId);
                                e.printStackTrace();
                                run_thread(conversationId);
                            }

                            @Override
                            public void onNext(Response<ConversationMessageInsertedSubscription.Data> response) {
                                if (!response.hasErrors()) {
                                    if (response.data().conversationMessageInserted() != null) {
                                        DataManager dataManager = DataManager.getInstance(ListenerService.this);
                                        if (!dataManager.getDataB("chat_is_going")) {
                                            Log.e(TAG, "dead");
                                            String chat_message = response.data().conversationMessageInserted().content();
                                            String name = "";
                                            try {
                                                if (response.data().conversationMessageInserted().user().details() != null)
                                                    name = response.data().conversationMessageInserted().user().details().fullName();
                                            } catch (Exception ignored) {
                                            }

                                            createNotificationChannel(chat_message, name, response.data().conversationMessageInserted().conversationId());
                                        }

                                        if (!config.conversationMessages.get(config.conversationMessages.size() - 1)._id
                                                .equals(ConversationMessage.convert(response.data().conversationMessageInserted())._id))
                                            config.conversationMessages.add(ConversationMessage.convert(response.data().conversationMessageInserted()));

                                        for (int i = 0; i < config.conversations.size(); i++) {
                                            if (config.conversations.get(i)._id.equals(response.data().conversationMessageInserted().conversationId())) {
                                                config.conversations.get(i).content = response.data().conversationMessageInserted().content();
                                                config.conversations.get(i).isread = false;
                                            }
                                        }
                                    }
                                }
                                Log.e(TAG, "onnext " + conversationId);
                            }

                            @Override
                            public void onComplete() {
                                Log.e(TAG, "subsrioption ehsouced");
                            }
                        }
                )
        );
    }

    private void createNotificationChannel(String chat_message, String name, String conversion_id) {


        Intent intent = new Intent(this, ErxesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

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
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "erxes";
            CharSequence name1 = "erxes_channel";
            String Description = "erxes notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name1, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("таньд мессеж ирлээ")
                    .setContentText(Html.fromHtml(chat_message))
                    .setSmallIcon(R.drawable.icon)
                    .setSound(alarmSound)
                    .setContentIntent(pendingIntent).getNotification();
            notificationManager.notify(123, notification);
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle("таньд мессеж ирлээ")
                    .setContentText(Html.fromHtml(chat_message))
                    .setSmallIcon(R.drawable.icon)
                    .setSound(alarmSound)
                    .setContentIntent(pendingIntent).getNotification();
            notificationManager.notify(123, notification);
        }

        Log.d(TAG, "notification can");
    }


}
