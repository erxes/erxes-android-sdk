package com.newmedia.erxeslibrary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.newmedia.erxeslibrary.Configuration.Config;

//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import javax.annotation.Nonnull;

public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    EditText email,phone;
    Button connect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = this.findViewById(R.id.email);
        phone = this.findViewById(R.id.phone);
        connect = this.findViewById(R.id.connect);
        connect.setOnClickListener(connect_click);
        Config.init(this,this.getIntent().getStringExtra("brandcode"));
        if(Config.isLoggedIn()){
            Config.LoggedInDefault();
            Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
            ErxesActivity.this.startActivity(a);
            finish();
        }
        if(Config.isNetworkConnected()){
            Toast.makeText(this,"connected",Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this,"not connected",Toast.LENGTH_SHORT).show();

    }
    public View.OnClickListener connect_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Config.setConnect(""+email.getText().toString(),phone.getText().toString());
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Config.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.add(this);
    }

    @Override
    public void notify(final boolean status,String conversationId) {
        if(status ) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ErxesActivity.this, "stats" + status, Toast.LENGTH_LONG).show();
                    Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                    ErxesActivity.this.startActivity(a);
                    finish();
                }
            });
        }

    }
}
