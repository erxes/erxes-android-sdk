package com.example.lol.uusususuasd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.newmedia.erxeslibrary.Configuration.Config;


public class MainActivity extends AppCompatActivity {
    Config config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        config  = Config.getInstance(this);
//        config.Init("yPv5aN",
//                "http://192.168.1.8:3100/graphql",
//                "ws://192.168.1.8:3300/subscriptions",
//                "http://192.168.1.8:3300/upload-file" );

        config  = Config.getInstance(this);
        config.Init("Wtmpph",
                "https://api.crm.nmma.co/graphql",
                "wss://app-api.crm.nmma.co/subscriptions",
                "https://api.crm.nmma.co/upload-file" );

        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                config.Start();
//              config.Start_login_email("orshih_bat@yahoo.com");
//              config.Start_login_phone("99001100");
            }
        });


    }
}