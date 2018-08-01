package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErrorType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;



public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    EditText email,phone;
    TextView  sms_button,email_button;
    LinearLayout container;
    ImageView connect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.Init(this);
        Config.LoadDefaultValues();
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
//        this.getSupportActionBar().hide();
        email = this.findViewById(R.id.email);
        phone = this.findViewById(R.id.phone);
        container = findViewById(R.id.linearlayout);
        connect = this.findViewById(R.id.connect);
        connect.setOnClickListener(connect_click);
        sms_button = this.findViewById(R.id.sms_button);
        email_button = this.findViewById(R.id.email_button);

        if(Config.color!=null){
//            this.findViewById(R.id.linearlayout).setBackgroundColor(Color.parseColor(Config.color));
            this.findViewById(R.id.info_header).setBackgroundColor(Color.parseColor(Config.color));
            email_button.setBackgroundColor(Color.parseColor(Config.color));
            sms_button.setTextColor(Color.parseColor(Config.color));
            connect.setBackgroundColor(Color.parseColor(Config.color));
        }
        if(Config.isLoggedIn()){
            Config.LoggedInDefault();
            Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
            ErxesActivity.this.startActivity(a);
            finish();
        }
        if(Config.isNetworkConnected()){
            if(Config.integrationId != null)
                ErxesRequest.isMessengerOnline(Config.integrationId);
        }
        else {

        }


    }
    public void email_click(View v){
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.GONE);

        if(Config.color!=null){
            email_button.setBackgroundColor(Color.parseColor(Config.color));
            email_button.setTextColor(Color.WHITE);
            sms_button.setBackgroundColor(Color.WHITE);
            sms_button.setTextColor(Color.parseColor(Config.color));
        }
        else{
            email_button.setBackgroundColor(Color.parseColor("#5629B6"));
            email_button.setTextColor(Color.WHITE);
            sms_button.setBackgroundColor(Color.WHITE);
            sms_button.setTextColor(Color.parseColor("#5629B6"));
        }
    }
    public void sms_click(View v){
        email.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);

        if(Config.color!=null){
            sms_button.setBackgroundColor(Color.parseColor(Config.color));
            sms_button.setTextColor(Color.WHITE);
            email_button.setBackgroundColor(Color.WHITE);
            email_button.setTextColor(Color.parseColor(Config.color));
        }
        else{
            sms_button.setBackgroundColor(Color.parseColor("#5629B6"));
            sms_button.setTextColor(Color.WHITE);
            email_button.setBackgroundColor(Color.WHITE);
            email_button.setTextColor(Color.parseColor("#5629B6"));
        }
    }
    public View.OnClickListener connect_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(Config.isNetworkConnected()) {
                ErxesRequest.setConnect("" + email.getText().toString(), phone.getText().toString());
            }
            else{
                Snackbar.make(container,R.string.cantconnect,Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ErxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ErxesRequest.add(this);
    }

    @Override
    public void notify(final boolean status,String conversationId,ErrorType errorType) {
        if(status ) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                    ErxesActivity.this.startActivity(a);
                    finish();
                }
            });
        }
        else {
            Snackbar.make(container,R.string.cantconnect,Snackbar.LENGTH_SHORT).show();
//            Toast.makeText(this,"амжилтгүй",Toast.LENGTH_SHORT).show();
        }

    }
}
