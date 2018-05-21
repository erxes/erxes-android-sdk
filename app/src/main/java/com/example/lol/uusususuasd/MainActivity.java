package com.example.lol.uusususuasd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.newmedia.erxeslibrary.*;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Api3100 connect = new Api3100();
//        connect.test();
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent a = new Intent(MainActivity.this,ErxesActivity.class);
                a.putExtra("brandcode","YDEdKj");
                startActivity(a);
            }
        });


    }
}
