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



public class Config implements ErxesObserver{


    //    final  public String HOST="192.168.1.6";
//    final  private String HOST="192.168.86.39";
    private String HOST="";
    public String HOST_3100="http://"+HOST+":3100/graphql";
    public String HOST_3300="ws://"+HOST+":3300/subscriptions";
    public String HOST_UPLOAD="http://"+HOST+":3300/upload-file";
    public String customerId;
    public String integrationId;
    private String color;
    public String language,wallpaper;;
    public String thankYouMessage;
    public String awayMessage;
    public String welcomeMessage;
    public String timezone;
    public String availabilityMethod;
    public int colorCode;
    public String conversationId=null; ///public
    public String brandCode;
    public boolean isMessengerOnline = false,notifyCustomer;
    private DataManager dataManager;
    private Context context;
    private ErxesRequest erxesRequest;
    static private Config config;
    public String convert_datetime(Long createDate) {
        Long diffTime = Calendar.getInstance().getTimeInMillis()  - createDate;

        diffTime = diffTime/1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        if(language == null || language.equalsIgnoreCase("en")){
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
    public String Message_datetime(String createDate_s) {

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
    @Override
    public void notify(ReturnType returnType, String conversationId, String message) {
        if(ReturnType.LOGIN_SUCCESS == returnType)
        {

        }
    }

    static public Config getInstance(Context context){
        if(config == null)
            config = new Config(context);

        return config;
    }
    private Config(Context context) {
        dataManager = DataManager.getInstance(context);
        this.context = context;
    }

    public void Init(String brandcode, String ip_3100,String ip_3300,String ip_upload_file){
//        HOST_3100="http://"+HOST+":3100/graphql";
//        HOST_3300="ws://"+HOST+":3300/subscriptions";
//        HOST_UPLOAD="http://"+HOST+":3300/upload-file";

        HOST_3100 = ip_3100;
        HOST_3300 = ip_3300;
        HOST_UPLOAD = ip_upload_file;
        this.brandCode  = brandcode;
        dataManager.setData("HOST3100",HOST_3100);
        dataManager.setData("HOST3300",HOST_3300);
        dataManager.setData("HOSTUPLOAD",HOST_UPLOAD);
        dataManager.setData("BRANDCODE",brandcode);
        erxesRequest =  ErxesRequest.getInstance(this.context);
        LoadDefaultValues();

    }
    public void Init(Context context){
        this.context = context;
        dataManager = DataManager.getInstance(context);
        HOST = dataManager.getDataS("HOST");
        HOST_3100 = dataManager.getDataS("HOST3100");
        HOST_3300 = dataManager.getDataS("HOST3300");
        HOST_UPLOAD = dataManager.getDataS("HOSTUPLOAD");
        brandCode = dataManager.getDataS("BRANDCODE");
        erxesRequest =  ErxesRequest.getInstance(this.context);
        LoadDefaultValues();

    }
    public void Start(){
        Intent a = new Intent(context,ErxesActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.push_down_in, R.anim.push_down_out);
            context.startActivity(a,options.toBundle());
        }
        else
            context.startActivity(a);

    }
    public void Start_login_email(String email){
        erxesRequest.add(this);
        erxesRequest.setConnect(email,"");
        Intent a = new Intent(context,ErxesActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.push_down_in, R.anim.push_down_out);
            context.startActivity(a,options.toBundle());
        }
        else
            context.startActivity(a);

    }
    public void Start_login_phone(String phone){
        erxesRequest.setConnect("",phone);
        Intent a = new Intent(context,ErxesActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.push_down_in, R.anim.push_down_out);
            context.startActivity(a,options.toBundle());
        }
        else
            context.startActivity(a);

    }
    public void LoadDefaultValues(){
        customerId = dataManager.getDataS(DataManager.customerId);
        integrationId = dataManager.getDataS(DataManager.integrationId);
        welcomeMessage = dataManager.getDataS("welcomeMessage");
        color= dataManager.getDataS(DataManager.color);
        if(color !=null)
            colorCode = Color.parseColor(color);
        else
            colorCode = Color.parseColor("#5629B6");
        wallpaper= dataManager.getDataS("wallpaper");
        language = dataManager.getDataS(DataManager.language);
        erxesRequest.changeLanguage(language);
    }

    public boolean isLoggedIn(){
        if(dataManager.getDataS(DataManager.customerId)==null)
            return false;
        return true;
    }
    public boolean Logout(){
        customerId = null;
        dataManager.setData(DataManager.customerId,null);
        dataManager.setData(DataManager.integrationId,null);
        return true;
    }
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean messenger_status_check(){
        if(isNetworkConnected()&& isMessengerOnline){
            return true;
        }
        return false;
    }
}
