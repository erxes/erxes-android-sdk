package com.newmedia.erxeslibrary.connection;

import android.content.Context;

import com.newmedia.erxeslibrary.configuration.Config;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetGEO {

    final static String TAG = "GetGEO";
    private Config config;

    public GetGEO(Context context) {
        config = Config.getInstance(context);
    }

    public void run() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url("https://geo.erxes.io")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    if (response.body() != null) {
                        config.geoResponse = response.body().string();
//                        config.geo = Geo.convert(response.body().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
