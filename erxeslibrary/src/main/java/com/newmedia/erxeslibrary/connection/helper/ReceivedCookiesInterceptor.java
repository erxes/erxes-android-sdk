package com.newmedia.erxeslibrary.connection.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {

    private Context context;
    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }
    @NonNull
    @Override
    public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>(originalResponse.headers("Set-Cookie"));
            if(cookies.size()>0){
                if(!cookies.iterator().next().contains("route")){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putStringSet("PREF_COOKIES", cookies)
                        .apply();
                }
            }

        } else if (!originalResponse.headers("set-cookie").isEmpty()){
            HashSet<String> cookies = new HashSet<>(originalResponse.headers("set-cookie"));
            if(cookies.size()>0){
                if(!cookies.iterator().next().contains("route")){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putStringSet("PREF_COOKIES", cookies)
                        .apply();
                }
            }
        }

        return originalResponse;
    }
}