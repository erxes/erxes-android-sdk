package com.newmedia.erxeslibrary.Configuration;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
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
import com.newmedia.erxeslibrary.ErxesActivity;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.R;

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
//    final static private String HOST="192.168.86.39";
    static private String HOST="";
    static public String HOST_3100="http://"+HOST+":3100/graphql";
    static public String HOST_3300="ws://"+HOST+":3300/subscriptions";
    static public String HOST_UPLOAD="http://"+HOST+":3300/upload-file";
    static  public String customerId;
    static  public String integrationId;
    static  private String color;
    static  public String language,wallpaper;;
    static  public String thankYouMessage;
    static  public String awayMessage;
    static  public String welcomeMessage;
    static  public String timezone;
    static  public String availabilityMethod;
    static  public  int colorCode;
    static  public String conversationId=null; ///public
    static  public String brandCode;
    static  public boolean isMessengerOnline = false,notifyCustomer;
    static private DataManager dataManager;
    static private Context context;
    static public String convert_datetime(Long createDate) {
        Long diffTime = Calendar.getInstance().getTimeInMillis()  - createDate;

        diffTime = diffTime/1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        if(Config.language == null || Config.language.equalsIgnoreCase("en")){
            if (weeks > 0) {
                return ("" + weeks + " weeks ago");
            } else if (days > 0) {
                return ("" + days + " d");
            } else if (hours > 0) {
                return ("" + hours + " h");
            } else if (minutes > 0) {
                return ("" + minutes + " m");
            } else {
                return ("" + seconds + " s");
            }
        }else {
            if (weeks > 0) {
                return ("" + weeks + " 7-хоногийн өмнө");
            } else if (days > 0) {
                return ("" + days + " өдрийн өмнө");
            } else if (hours > 0) {
                return ("" + hours + " цагийн өмнө");
            } else if (minutes > 0) {
                return ("" + minutes + " минутын өмнө");
            } else {
                return ("" + seconds + " секундын өмнө");
            }
        }
    }
    static  public String Message_datetime(String createDate_s) {

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
    static public void Init(Context context,String brandcode,String ip){
        dataManager =  DataManager.getInstance(context);
        Config.HOST = ip;
        HOST_3100="http://"+HOST+":3100/graphql";
        HOST_3300="ws://"+HOST+":3300/subscriptions";
        HOST_UPLOAD="http://"+HOST+":3300/upload-file";

        dataManager.setData("HOST",ip);
        dataManager.setData("HOST3100",HOST_3100);
        dataManager.setData("HOST3300",HOST_3300);
        dataManager.setData("BRANDCODE",brandcode);


        ErxesRequest.init(context);
        Config.context = context;
        Config.brandCode = brandcode;

    }
    static public void Init(Context context){
        if(dataManager != null)
            return;
        dataManager =  DataManager.getInstance(context);
        Config.HOST = dataManager.getDataS("HOST");
        HOST_3100="http://"+HOST+":3100/graphql";
        HOST_3300="ws://"+HOST+":3300/subscriptions";
        HOST_UPLOAD="http://"+HOST+":3300/upload-file";



        ErxesRequest.init(context);
        Config.context = context;
        Config.brandCode = dataManager.getDataS("BRANDCODE");

    }
    static public void Start(){
        Intent a = new Intent(context,ErxesActivity.class);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.push_down_in, R.anim.push_down_out);
            context.startActivity(a,options.toBundle());
        }
        else
            context.startActivity(a);

    }
    static public void LoadDefaultValues(){

        Config.integrationId = dataManager.getDataS(DataManager.integrationId);
        Config.welcomeMessage = dataManager.getDataS("welcomeMessage");
        Config.color= dataManager.getDataS(DataManager.color);
        if(Config.color !=null)
            Config.colorCode = Color.parseColor(Config.color);
        else
            Config.colorCode = Color.parseColor("#5629B6");
        Config.wallpaper= dataManager.getDataS("wallpaper");
        Config.language = dataManager.getDataS(DataManager.language);
        ErxesRequest.changeLanguage(Config.language);
    }
    static public void LoggedInDefault(){
        Config.customerId = dataManager.getDataS(DataManager.customerId);
        LoadDefaultValues();

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

    static public boolean messenger_status_check(){
        if(Config.isNetworkConnected()&& Config.isMessengerOnline){
            return true;
        }
        return false;
    }







}
