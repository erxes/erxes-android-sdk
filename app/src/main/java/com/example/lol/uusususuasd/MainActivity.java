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
//        Api3100 connect = new Api3100();
//        connect.test();
//        Config.Init(this,"yPv5aN","192.168.1.15");
//        Config config = Config.getInstance(this);
//        config.Init("","","","");
//        config.Start_login_email("orshih_bat@uahoo.com");
//        Config.Init(this,"YDEdKj","192.168.1.6");
//        Config.Init(this,"yPv5aN","172.20.10.3");
//        Config.Init(this,"yPv5aN","192.168.100.185");
        config  = Config.getInstance(this);
        config.Init("yPv5aN",
                "http://192.168.50.57:3100/graphql",
                "ws://192.168.50.57:3300/subscriptions",
                "http://192.168.50.57:3300/upload-file" );
//        config.Init("yPv5aN",
//                "http://192.168.100.185:3100/graphql",
//                "ws://192.168.100.185:3300/subscriptions",
//                "http://192.168.100.185:3300/upload-file" );

//        HOST_3100="http://"+HOST+":3100/graphql";
//        HOST_3300="ws://"+HOST+":3300/subscriptions";
//        HOST_UPLOAD="http://"+HOST+":3300/upload-file";

//        Config.Init(this,"yPv5aN","192.168.86.43");





//        Intent a  = new Intent(this,DeleteActivity.class);
//        startActivity(a);
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                config.Start();
                config.Start_login_email("orshih_bat@yahoo.com");
            }
        });


    }
}