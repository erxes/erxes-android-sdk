package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.ErrorType;
import com.newmedia.erxeslibrary.Configuration.ErxesRequest;



public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    EditText email,phone;
    TextView connect;
    LinearLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config.LoadDefaultValues();
        setContentView(R.layout.activity_login);
        this.getSupportActionBar().hide();
        email = this.findViewById(R.id.email);
        phone = this.findViewById(R.id.phone);
        container = findViewById(R.id.linearlayout);
        connect = this.findViewById(R.id.connect);
        connect.setOnClickListener(connect_click);

        if(Config.color!=null){
            this.findViewById(R.id.linearlayout).setBackgroundColor(Color.parseColor(Config.color));
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
            Toast.makeText(this,"амжилтгүй",Toast.LENGTH_SHORT).show();
        }

    }
}
