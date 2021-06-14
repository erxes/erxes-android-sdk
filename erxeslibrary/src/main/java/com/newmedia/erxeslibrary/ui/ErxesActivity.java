package com.newmedia.erxeslibrary.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    private EditText email, phone;
    private TextView smsButton;
    private TextView emailButton, contact;
    private LinearLayout container;
    private ImageView mailImageView, phoneImageView, sendImageView, cancelImageView;
    private CardView mailCardView, smsCardView;
    private Config config;
    private ErxesRequest erxesRequest;
    private DataManager dataManager;
    private LinearLayout loaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        dataManager = DataManager.getInstance(this);
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ErxesHelper.changeLanguage(this, config.language);

        setContentView(R.layout.activity_erxes);

        loaderView = this.findViewById(R.id.loaderView);
        email = this.findViewById(R.id.email);
        phone = this.findViewById(R.id.phone);
        container = findViewById(R.id.linearlayout);
        smsButton = this.findViewById(R.id.sms_button);
        emailButton = this.findViewById(R.id.email_button);
        mailCardView = this.findViewById(R.id.mailgroup);
        smsCardView = this.findViewById(R.id.smsgroup);
        mailImageView = this.findViewById(R.id.mail_zurag);
        phoneImageView = this.findViewById(R.id.phonezurag);
        sendImageView = this.findViewById(R.id.sendImageView);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        contact = this.findViewById(R.id.contact);

        contact.setTextColor(config.textColorCode);

        config.setCursorColor(email,config.colorCode);
        config.setCursorColor(phone,config.colorCode);

        ErxesHelper.display_configure(this, container, "#66000000");
        change_color();
        cancelImageView.setOnClickListener(touchListener);
        initIcon();

        init();
    }

    private void init() {
        if (config.isUser()) {
            erxesRequest.setConnect( false, true);
        } else {
            change_color();
            contact.setVisibility(View.VISIBLE);
            loaderView.setVisibility(View.GONE);
        }
    }

    private void initIcon() {
        Glide.with(this).load(config.getsendIcon(this, 0)).into(sendImageView);
        Glide.with(this).load(config.getCancelIcon(config.textColorCode)).into(cancelImageView);
        Glide.with(this)
                .load(config.getEmailIcon(this, getResources().getColor(R.color.md_white_1000)))
                .into(mailImageView);
        Glide.with(this)
                .load(config.getPhoneIcon(this, getResources().getColor(R.color.md_white_1000)))
                .into(phoneImageView);
    }

    private void changeEmailColor(int color) {
        Glide.with(this).load(config.getEmailIcon(this, color)).into(mailImageView);
    }

    private void changePhoneColor(int color) {
        Glide.with(this).load(config.getPhoneIcon(this, color)).into(phoneImageView);
    }

    private void change_color() {
        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        email_click(null);
    }

    public void email_click(View v) {
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.GONE);

        mailCardView.setCardBackgroundColor(config.colorCode);
        smsCardView.setCardBackgroundColor(getResources().getColor(R.color.md_white_1000));
        emailButton.setTextColor(config.textColorCode);
        smsButton.setTextColor(config.getInColor(getResources().getColor(R.color.md_white_1000)));
        changeEmailColor(config.textColorCode);
        changePhoneColor(config.getInColor(getResources().getColor(R.color.md_white_1000)));
    }

    public void sms_click(View v) {
        email.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);

        smsCardView.setCardBackgroundColor(config.colorCode);
        mailCardView.setCardBackgroundColor(getResources().getColor(R.color.md_white_1000));
        smsButton.setTextColor(config.textColorCode);
        emailButton.setTextColor(config.getInColor(getResources().getColor(R.color.md_white_1000)));
        changePhoneColor(config.textColorCode);
        changeEmailColor(config.getInColor(getResources().getColor(R.color.md_white_1000)));
    }

    public void logout() {
        this.finish();
    }

    public void Connect_click(View v) {
        if (config.isNetworkConnected()) {
            View view = this.getCurrentFocus();
            if (email.getVisibility() == View.GONE) {
                if (phone.getText().toString().length() > 7) {
                    dataManager.setData(DataManager.PHONE, phone.getText().toString());
                    config.phone = phone.getText().toString();
                    erxesRequest.setConnect(false,false);
                    contact.setVisibility(View.GONE);
                    loaderView.setVisibility(View.VISIBLE);

                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    phone.setError(null);
                } else
                    phone.setError(ErxesHelper.getLocalizedResources(this,config.language).getString(R.string.Failed));
            } else {
                if (config.isValidEmail(email.getText().toString())) {
                    email.setError(null);
                    dataManager.setData(DataManager.EMAIL, email.getText().toString());
                    config.email = email.getText().toString();
                    erxesRequest.setConnect( false, false);
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    contact.setVisibility(View.GONE);
                    loaderView.setVisibility(View.VISIBLE);
                } else
                    email.setError(ErxesHelper.getLocalizedResources(this,config.language).getString(R.string.Failed));
            }
        } else {
            Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
        config.setActivityConfig(this);
    }

    @Override
    public void notify(int returnType, String conversationId, String message, Object object) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturntypeUtil.LOGINSUCCESS:
                        Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                        ErxesActivity.this.startActivity(a);
                        ErxesActivity.this.finish();
                        break;

                    case ReturntypeUtil.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                        contact.setVisibility(View.VISIBLE);
                        loaderView.setVisibility(View.GONE);
                        break;

                    case ReturntypeUtil.SERVERERROR:
                        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
                        contact.setVisibility(View.VISIBLE);
                        loaderView.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });


    }

    private final View.OnClickListener touchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancelImageView)
                logout();
        }
    };

}
