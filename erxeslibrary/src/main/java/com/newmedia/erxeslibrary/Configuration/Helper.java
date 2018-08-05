package com.newmedia.erxeslibrary.Configuration;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.widget.ImageView;

import com.newmedia.erxeslibrary.DataManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Helper {
    static DataManager dataManager;
    static void Init(Context context){
        dataManager =  DataManager.getInstance(context);
    }
    static public void load_uiOptions(JSONObject js){
        if(js == null)
            return;
        String color = null;
        try {
            color = js.getString("color");
            dataManager.setData(DataManager.color, color);
            if(color != null)
                Config.colorCode = Color.parseColor(color);
            else{
                Config.colorCode = Color.parseColor("#5629B6");
            }
        }catch (JSONException e){
        }
        try {
            color = js.getString("wallpaper");
            dataManager.setData("wallpaper", color);
        }catch (JSONException e){
        }

    }
    static public void load_messengerData(JSONObject js){
        if(js == null)
            return;
        String temp = null;
        try {
            temp = js.getString("thankYouMessage");
            dataManager.setData("thankYouMessage", temp);
            Config.thankYouMessage = temp;
        } catch (JSONException e) {
        }

        try {
            temp = js.getString("awayMessage");
            dataManager.setData("awayMessage", temp);
            Config.awayMessage = temp;
        } catch (JSONException e) {
        }
        try {
            temp = js.getString("welcomeMessage");
            dataManager.setData("welcomeMessage", temp);
            Config.welcomeMessage = temp;
        } catch (JSONException e) {
        }
        try {
            temp = js.getString("timezone");
            dataManager.setData("timezone", temp);
            Config.timezone = temp;
        } catch (JSONException e) {
        }
        try {
            temp = js.getString("availabilityMethod");
            dataManager.setData("availabilityMethod", temp);
            Config.availabilityMethod = temp;
        } catch (JSONException e) {
        }
        try {
            boolean bool = js.getBoolean("isOnline");
            dataManager.setData("isOnline", bool);
            Config.isMessengerOnline = bool;
        } catch (JSONException e) {
        }

        try {
            boolean bool = js.getBoolean("notifyCustomer");
            dataManager.setData("notifyCustomer", bool);
            Config.notifyCustomer = bool;
        } catch (JSONException e) {
        }
    }

}
