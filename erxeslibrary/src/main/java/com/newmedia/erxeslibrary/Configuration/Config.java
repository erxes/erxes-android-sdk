package com.newmedia.erxeslibrary.Configuration;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;




public class Config implements ErxesObserver{

    public String HOST_3100="";
    public String HOST_3300="";
    public String HOST_UPLOAD="";
    public String customerId;
    public String integrationId;
    private String color;
    public String language,wallpaper;
    public Messengerdata messengerdata;
    public int colorCode;
    public String conversationId=null;
    public String brandCode;
    public boolean isMessengerOnline = false,notifyCustomer;
    private DataManager dataManager;
    public Context context;
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
        if(language.equalsIgnoreCase("mn")){
            format3 = new SimpleDateFormat("MMM сарын d,HH:mm");
            format2 = format3;
        }
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
    public String now() {


        Date date = new Date();

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd / yyyy HH:mm");


        if(this.language.equalsIgnoreCase("en")){
            return format2.format(date);
        }
        else {
            return format.format(date);
        }


    }
    @Override
    public void notify(int returnType, String conversationId, String message) {
        if(ReturnType.LOGIN_SUCCESS == returnType)
        {
            Intent a = new Intent(context,ErxesActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.push_down_in, R.anim.push_down_out);
                context.startActivity(a,options.toBundle());
            }
            else
                context.startActivity(a);
        }
    }

    static public Config getInstance(Context context){
        if(config == null) {
            config = new Config(context);
            config.erxesRequest = ErxesRequest.getInstance(config);
            if(config.HOST_3100!=null)
                config.erxesRequest.set_client();
        }
        return config;
    }
    private Config(Context context) {
        dataManager = DataManager.getInstance(context);
        this.context = context;
        LoadDefaultValues();
    }

    private void Init(String brandcode, String ip_3100,String ip_3300,String ip_upload_file){
        HOST_3100 = ip_3100;
        HOST_3300 = ip_3300;
        HOST_UPLOAD = ip_upload_file;
        this.brandCode  = brandcode;
        dataManager.setData("HOST3100",HOST_3100);
        dataManager.setData("HOST3300",HOST_3300);
        dataManager.setData("HOSTUPLOAD",HOST_UPLOAD);
        dataManager.setData("BRANDCODE",brandcode);
        LoadDefaultValues();
        erxesRequest.set_client();

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


    }
    public void Start_login_phone(String phone){
        erxesRequest.add(this);
        erxesRequest.setConnect("",phone);
    }
    public void LoadDefaultValues(){

        dataManager = DataManager.getInstance(context);
        HOST_3100 = dataManager.getDataS("HOST3100");
        HOST_3300 = dataManager.getDataS("HOST3300");
        HOST_UPLOAD = dataManager.getDataS("HOSTUPLOAD");
        brandCode = dataManager.getDataS("BRANDCODE");

        customerId = dataManager.getDataS(DataManager.customerId);
        integrationId = dataManager.getDataS(DataManager.integrationId);
        messengerdata = dataManager.getMessenger();
        color= dataManager.getDataS(DataManager.color);
        if(color !=null)
            colorCode = Color.parseColor(color);
        else
            colorCode = Color.parseColor("#5629B6");
        wallpaper= dataManager.getDataS("wallpaper");
        language = dataManager.getDataS(DataManager.language);
        changeLanguage(language);

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
    public void changeLanguage(String lang) {
        if(lang == null || lang.equalsIgnoreCase("") )
            return;

        this.language = lang ;
        dataManager.setData(DataManager.language, this.language);

        Locale myLocale;
        myLocale = new Locale(lang);

        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

    }
    public static class	Builder	{
        private String brand;
        private String apiHost;
        private String subscriptionHost;
        private String uploadHost;
        public Builder(@NonNull String brand) {

            this.brand = brand;
        }
        public Builder setApiHost(String apiHost) {
            this.apiHost = apiHost;
            return this;
        }
        public Builder setSubscriptionHost(String subscriptionHost) {
            this.subscriptionHost = subscriptionHost;
            return this;
        }

        public Builder setUploadHost(String uploadHost) {
            this.uploadHost = uploadHost;
            return this;
        }
        public Config build(Context context1)	{
            Config t = Config.getInstance(context1);
            t.Init(this.brand,this.apiHost,this.subscriptionHost,this.uploadHost);
            return t;
        }
    }
}
