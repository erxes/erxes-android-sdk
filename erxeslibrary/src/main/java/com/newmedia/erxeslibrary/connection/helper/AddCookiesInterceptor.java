package com.newmedia.erxeslibrary.connection.helper;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddCookiesInterceptor implements Interceptor {

    private Context context;
    public AddCookiesInterceptor(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Interceptor.Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        HashSet<String> preferences = (HashSet<String>) PreferenceManager
                .getDefaultSharedPreferences(context)
                .getStringSet("PREF_COOKIES", new HashSet<String>());

        assert preferences != null;
        for (String cookie : preferences) {

            if(!cookie.contains("route")) {
                Log.d("cookietest",""+cookie);
                builder.addHeader("Cookie", cookie);
            }

        }

        return chain.proceed(builder.build());
    }
}