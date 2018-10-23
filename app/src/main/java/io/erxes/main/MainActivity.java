package io.erxes.main;

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

//        config  = new Config.Builder("yPv5aN")
//                .setApiHost("http://192.168.1.31:3100/graphql")
//                .setSubscriptionHost("ws://192.168.1.31:3300/subscriptions")
//                .setUploadHost("http://192.168.1.31:3300/upload-file").build(this);




//
//        config  = Config.getInstance(this);
//        config.Init("Wtmpph",
//                "https://api.crm.nmma.co/graphql",
//                "wss://app-api.crm.nmma.co/subscriptions",
//                "https://api.crm.nmma.co/upload-file" );
        config = new Config.Builder("Wtmpph")
                .setApiHost("https://wapi.crm.nmma.co/graphql")
                .setSubscriptionHost("wss://api.crm.nmma.co/subscriptions")
                .setUploadHost("https://api.crm.nmma.co/upload-file")
                .build(this);
//        config = new Config.Builder("yPv5aN")
//                .setApiHost("http://192.168.86.120:3100/graphql")
//                .setSubscriptionHost("ws://192.168.86.120:3300/subscriptions")
//                .setUploadHost("http://192.168.86.120:3300/upload-file")
//                .build(this);
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