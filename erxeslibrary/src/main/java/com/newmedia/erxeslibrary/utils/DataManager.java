package com.newmedia.erxeslibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.newmedia.erxeslibrary.helper.Json;
import com.newmedia.erxeslibrary.model.Messengerdata;

import java.util.Map;

/**
 * Created by lol on 3/23/16.
 */
public final class DataManager {

    private static DataManager dataManager;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final String PREFER_NAME = "ERXES_LIB";
    public static final String EMAIL = "rxx_email";
    public static final String PHONE = "rxx_phone";
    public static final String INTEGRATIONID = "rxx_integrationId";
    public static final String CUSTOMERID = "rxx_customerId";
    public static final String COLOR = "rxx_color";
    public static final String LANGUAGE = "rxx_language";
    public static final String ISUSER = "rxx_isUser";
    public static final String CUSTOMDATA = "rxx_customData";
    public static final String HASPROVIDER = "rxx_hasprovider";

    public static DataManager getInstance(Context context) {
        if (dataManager == null)
            dataManager = new DataManager(context);
        return dataManager;
    }

    private DataManager(Context context) {
        pref = context.getSharedPreferences(PREFER_NAME, 0);
        editor = pref.edit();
    }

    public void setData(String key, String data) {
        // Storing login value as TRUE
        editor.putString(key, data);
        editor.apply();
    }

    public String getDataS(String key) {
        // Storing login value as TRUE
        return pref.getString(key, null);
    }

    public void setData(String key, int data) {
        // Storing login value as TRUE
        editor.putInt(key, data);
        editor.apply();
    }

    public int getDataI(String key) {
        // Storing login value as TRUE
        return pref.getInt(key, 0);
    }

    public void setData(String key, boolean data) {
        // Storing login value as TRUE
        editor.putBoolean(key, data);
        editor.apply();
    }

    public boolean getDataB(String key) {
        // Storing login value as TRUE
        return pref.getBoolean(key, false);
    }

    public void setMessengerData(String data) {
        editor.putString("message", data);
        editor.apply();
    }

    public Messengerdata getMessenger() {
        if (pref.getString("message", null) != null) {
            Gson gson = new Gson();

            Map jsonMap = gson.fromJson(pref.getString("message", null), Map.class);
            return Messengerdata.convert(
                    new Json(jsonMap),
                    pref.getString(LANGUAGE, null)
            );
        } else
            return null;
    }
}
