package com.newmedia.erxeslibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.newmedia.erxeslibrary.model.Messengerdata;
import com.newmedia.erxeslibrary.helper.Json;

import org.json.JSONException;
import org.json.JSONObject;

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


    public static DataManager getInstance(Context context){
        if(dataManager == null)
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
        return pref.getBoolean(key, true);
    }
    public void setMessengerData(String data){
        editor.putString("message", data);
        editor.apply();
    }
    public Messengerdata getMessenger(){
        if (pref.getString("message", null) != null) {
            try {
                return Messengerdata.convert(new Json(new JSONObject(pref.getString("message", null))), pref.getString(LANGUAGE, null));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }
}
