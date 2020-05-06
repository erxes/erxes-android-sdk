package com.newmedia.erxeslibrary.connection.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor {

    private Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException, NullPointerException {
        Response originalResponse = chain.proceed(chain.request());
        HashSet<String> cookies = new HashSet<>();
        if (!originalResponse.headers("Set-cookie").isEmpty()) {
            for (String header : originalResponse.headers("Set-Cookie")) {
                cookies.add(header);
            }
            try {
                SharedPreferences.Editor memes = PreferenceManager.getDefaultSharedPreferences(context).edit();
                memes.putStringSet("PREF_COOKIES_ERXESSDK_ANDROID", cookies).apply();
                memes.commit();
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
        }
        return originalResponse;
    }
}