package com.newmedia.erxeslibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.newmedia.erxeslibrary.Configuration.Messengerdata;

/**
 * Created by lol on 3/23/16.
 */
public class DataManager {
    SharedPreferences pref;
    int PRIVATE_MODE = 0;
    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;
    private static final String PREFER_NAME = "ERXES_LIB";
    public static final String brandcode = "brandcode";
    public static final String email = "email";
    public static final String phone = "phone";
    public static final String integrationId = "integrationId";
    public static final String customerId = "customerId";
    public static final String color = "color";
    public static final String language = "language";


    static private DataManager dataManager;
    static public DataManager getInstance(Context context){
        if(dataManager == null)
            dataManager = new DataManager(context);

        return dataManager;
    }
    // Context
    Context _context;
    private DataManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    public void setData(String key, String data) {
        // Storing login value as TRUE
        editor.putString(key, data);
        editor.commit();
    }
    public String getDataS(String key) {
        // Storing login value as TRUE
        return pref.getString(key, null);
    }
    public void setData(String key, int data) {
        // Storing login value as TRUE
        editor.putInt(key, data);
        editor.commit();
    }
    public int getDataI(String key) {
        // Storing login value as TRUE
        return pref.getInt(key, 0);
    }
    public void setData(String key, boolean data) {
        // Storing login value as TRUE
        editor.putBoolean(key, data);
        editor.commit();
    }
    public boolean getDataB(String key) {
        // Storing login value as TRUE
        return pref.getBoolean(key, true);
    }
    public void setMessengerData(String data){
        editor.putString("message", data);
        editor.commit();
    }
    public Messengerdata getMessenger(){
        String a = pref.getString("message", null);
        Gson gson = new Gson();
        if( a != null )
            try {
                return gson.fromJson(a,Messengerdata.class);
            }
            catch (Exception e){}

        return null;
    }
}
