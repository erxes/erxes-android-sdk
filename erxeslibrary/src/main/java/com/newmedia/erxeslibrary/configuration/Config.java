package com.newmedia.erxeslibrary.configuration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.newmedia.erxes.basic.type.FieldValueInput;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.FormConnect;
import com.newmedia.erxeslibrary.model.Geo;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.ui.login.ErxesActivity;
import com.newmedia.erxeslibrary.ErxesObserver;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Config implements ErxesObserver {

    public String HOST_3100 = "";
    public String HOST_3300 = "";
    public String HOST_UPLOAD = "";
    public String customerId;
    public String integrationId;
    private String color;
    public String language, wallpaper;
    public Messengerdata messengerdata;
    public int colorCode;
    public String conversationId = null;
    public String brandCode;
    public boolean isMessengerOnline = false, notifyCustomer;
    private DataManager dataManager;
    public Activity activity;
    public Context context;
    private ErxesRequest erxesRequest;
    static private Config config;
    public FormConnect formConnect;
    public List<FieldValueInput> fieldValueInputs = new ArrayList<>();
    public Geo geo;
    public String geoResponse;
    public KnowledgeBaseTopic knowledgeBaseTopic = new KnowledgeBaseTopic();
    public List<User> supporters = new ArrayList<>();
    public List<Conversation> conversations = new ArrayList<>();
    public List<ConversationMessage> conversationMessages = new ArrayList<>();
    public boolean isFirstStart = false;

    public String convert_datetime(Long createDate) {
        Long diffTime = Calendar.getInstance().getTimeInMillis() - createDate;

        diffTime = diffTime / 1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        if (language == null || language.equalsIgnoreCase("en")) {
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
        } else {
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
            createDate = Long.valueOf(createDate_s);
        } catch (NumberFormatException e) {
            return "";
        }


        Date date = new Date();
        date.setTime(createDate);

        long diffTime = Calendar.getInstance().getTimeInMillis() - createDate;

        diffTime = diffTime / 1000;
        long weeks = diffTime / 604800;
        long days = (diffTime % 604800) / 86400;
        long hours = ((diffTime % 604800) % 86400) / 3600;
        long minutes = (((diffTime % 604800) % 86400) % 3600) / 60;
        long seconds = (((diffTime % 604800) % 86400) % 3600) % 60;
        SimpleDateFormat format =
                new SimpleDateFormat("h:mm a");
        SimpleDateFormat format2 =
                new SimpleDateFormat("EEE h:mm a");
        SimpleDateFormat format3 =
                new SimpleDateFormat("MMM d,h:mm a");
        if (language.equalsIgnoreCase("mn")) {
            format3 = new SimpleDateFormat("MMM сарын d,HH:mm");
            format2 = format3;
        }
        if (weeks > 0) {
            return format3.format(date);
        } else if (days > 0) {
            return format2.format(date);
        } else {
            return format.format(date);
        }
    }

    public String now() {


        Date date = new Date();

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd, yyyy h:mm a");


        if (this.language.equalsIgnoreCase("en")) {
            return format2.format(date);
        } else {
            return format.format(date);
        }


    }

    public String full_date(String createDate_s) {

        Long createDate;
        try {
            createDate = Long.valueOf(createDate_s);
        } catch (NumberFormatException e) {
            return "";
        }


        Date date = new Date();
        date.setTime(createDate);

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy оны MM сарын d, HH:mm");
        SimpleDateFormat format2 =
                new SimpleDateFormat("MMM dd / yyyy HH:mm");
        if (this.language.equalsIgnoreCase("en")) {
            return format2.format(date);
        } else {
            return format.format(date);
        }
    }

    public String conversationDate(long createdDate) {
        Date date = new Date();
        date.setTime(createdDate);

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(date);
    }

    @Override
    public void notify(int returnType, String conversationId, String message) {
        if (ReturnType.LOGIN_SUCCESS == returnType) {
            Intent a = new Intent(activity, ErxesActivity.class);
            activity.startActivity(a);
        }
    }

    static public Config getInstance(Activity activity) {
        if (config == null) {
            config = new Config(activity);
            config.erxesRequest = ErxesRequest.getInstance(config);
            if (config.HOST_3100 != null)
                config.erxesRequest.set_client();
        }
        return config;
    }
    static public Config getInstance(Context context) {
        if (config == null) {
            config = new Config(context);
            config.erxesRequest = ErxesRequest.getInstance(config);
            if (config.HOST_3100 != null)
                config.erxesRequest.set_client();
        }
        return config;
    }

    private Config(Activity activity) {
        dataManager = DataManager.getInstance(activity);
        this.activity = activity;
        LoadDefaultValues();
    }
    private Config(Context context) {
        dataManager = DataManager.getInstance(context);
        this.context = context;
        LoadDefaultValues();
    }

    private void Init(String brandcode, String ip_3100, String ip_3300, String ip_upload_file) {
        HOST_3100 = ip_3100;
        HOST_3300 = ip_3300;
        HOST_UPLOAD = ip_upload_file;
        this.brandCode = brandcode;
        dataManager.setData("HOST3100", HOST_3100);
        dataManager.setData("HOST3300", HOST_3300);
        dataManager.setData("HOSTUPLOAD", HOST_UPLOAD);
        dataManager.setData("BRANDCODE", brandcode);
        LoadDefaultValues();
        erxesRequest.set_client();

    }

    public void Start() {
        dataManager.setData(DataManager.isUser, false);
        dataManager.setData(DataManager.email, null);
        dataManager.setData(DataManager.phone, null);
        dataManager.setData(DataManager.customData, null);
        Intent a = new Intent(activity, ErxesActivity.class);
        a.putExtra("hasData",false);
        activity.startActivity(a);
    }

    public void Start(String email, String phone, JSONObject jsonObject) {
        dataManager.setData(DataManager.isUser, true);
        dataManager.setData(DataManager.email, email);
        dataManager.setData(DataManager.phone, phone);
        dataManager.setData(DataManager.customData, jsonObject.toString());
        Intent a = new Intent(activity, ErxesActivity.class);
        a.putExtra("hasData",true);
        a.putExtra("customData", jsonObject.toString());
        a.putExtra("mEmail",email);
        a.putExtra("mPhone",phone);
        activity.startActivity(a);
//        erxesRequest.add(this);

    }

//    public void Start_login_email(String email, boolean isUser) {
//        erxesRequest.add(this);
//        erxesRequest.setConnect(email, "", isUser, true);
//    }
//
//    public void Start_login_phone(String phone, boolean isUser) {
//        erxesRequest.add(this);
//        erxesRequest.setConnect("", phone, isUser, true);
//    }

    public void LoadDefaultValues() {

        dataManager = DataManager.getInstance(activity);
        HOST_3100 = dataManager.getDataS("HOST3100");
        HOST_3300 = dataManager.getDataS("HOST3300");
        HOST_UPLOAD = dataManager.getDataS("HOSTUPLOAD");
        brandCode = dataManager.getDataS("BRANDCODE");

        customerId = dataManager.getDataS(DataManager.customerId);
        integrationId = dataManager.getDataS(DataManager.integrationId);
        messengerdata = dataManager.getMessenger();
        color = dataManager.getDataS(DataManager.color);
        if (color != null)
            colorCode = Color.parseColor(color);
        else
            colorCode = Color.parseColor("#5629B6");
        wallpaper = dataManager.getDataS("wallpaper");
        language = dataManager.getDataS(DataManager.language);
        changeLanguage(language);

    }

    public boolean isLoggedIn() {
        return dataManager.getDataS(DataManager.customerId) != null;
    }

    public boolean Logout() {
        customerId = null;
        dataManager.setData(DataManager.customerId, null);
        dataManager.setData(DataManager.integrationId, null);
        return true;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean messenger_status_check() {
        return isNetworkConnected() && isMessengerOnline;
    }

    public void changeLanguage(String lang) {
        if (lang == null || lang.equalsIgnoreCase(""))
            return;

        this.language = lang;
        dataManager.setData(DataManager.language, this.language);

        Locale myLocale;
        myLocale = new Locale(lang);

        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        activity.getResources().updateConfiguration(config,
                activity.getResources().getDisplayMetrics());

    }

    public static class Builder {
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

        public Config build(Activity context1) {
            Config t = Config.getInstance(context1);
            t.Init(this.brand, this.apiHost, this.subscriptionHost, this.uploadHost);
            return t;
        }
    }
}
