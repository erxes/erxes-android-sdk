package com.newmedia.erxeslibrary.graphqlfunction;

import android.app.Activity;
import android.util.Log;

import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.model.Geo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class GetGEO {

    final static String TAG = "GetGEO";
    private ErxesRequest ER;
    private Config config;
    private DataManager dataManager;
    private Activity activity;

    public GetGEO(ErxesRequest ER, Activity context) {
        this.ER = ER;
        this.activity = context;
        config = Config.getInstance(context);
        dataManager = DataManager.getInstance(context);
    }

    public void run() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging).build();
        Request request = new Request.Builder()
                .url("https://geo.erxes.io")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (response.body() != null) {
                                config.geoResponse = response.body().string();
//                                    config.geo = Geo.convert(response.body().string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
