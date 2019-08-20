package com.newmedia.erxeslibrary.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.DataManager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesHelper;
import com.newmedia.erxeslibrary.configuration.Returntype;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.R;

public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    private EditText email, phone;
    private TextView smsButton;
    private TextView emailButton;
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
        dataManager = DataManager.getInstance(this);
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ErxesHelper.changeLanguage(this,config.language);
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
        TextView contact = this.findViewById(R.id.contact);

        ErxesHelper.display_configure(this, container, "#66000000");
        change_color();
        cancelImageView.setOnClickListener(touchListener);
        initIcon();

        boolean hasData = getIntent().getBooleanExtra("hasData", false);
        String customData = getIntent().getStringExtra("customData");
        String mEmail = getIntent().getStringExtra("mEmail");
        String mPhone = getIntent().getStringExtra("mPhone");
        if (hasData) {
            erxesRequest.setConnect(mEmail, mPhone, true, customData);
        } else {
            if (config.isLoggedIn()) {
                config.LoadDefaultValues();
                Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                a.putExtra("isFromLogin", false);
                ErxesActivity.this.startActivity(a);
                finish();
            } else {
                change_color();
                contact.setVisibility(View.VISIBLE);
                loaderView.setVisibility(View.GONE);
            }
        }
    }

    private void initIcon() {
        Glide.with(this).load(config.getsendIcon(this, 0)).into(sendImageView);
        Glide.with(this).load(config.getCancelIcon(this, config.getInColor(config.colorCode))).into(cancelImageView);
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
        mailCardView.setCardBackgroundColor(config.colorCode);
        smsButton.setTextColor(config.colorCode);
        changePhoneColor(config.colorCode);

        Drawable drawable = this.findViewById(R.id.selector).getBackground();
        drawable.setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);

    }

    public void email_click(View v) {
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.GONE);

        smsCardView.setCardBackgroundColor(Color.WHITE);
        emailButton.setTextColor(Color.WHITE);
        changeEmailColor(getResources().getColor(R.color.md_white_1000));
        smsButton.setTextColor(config.colorCode);
        ((CardView) v).setCardBackgroundColor(config.colorCode);
        changePhoneColor(config.colorCode);
    }

    public void sms_click(View v) {
        email.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);

        mailCardView.setCardBackgroundColor(Color.WHITE);
        smsButton.setTextColor(Color.WHITE);
        changePhoneColor(getResources().getColor(R.color.md_white_1000));
        emailButton.setTextColor(config.colorCode);
        ((CardView) v).setCardBackgroundColor(config.colorCode);
        changeEmailColor(config.colorCode);
    }

    public void logout() {
        this.finish();
    }

    public void Connect_click(View v) {
        if (config.isNetworkConnected()) {
            if (email.getVisibility() == View.GONE) {
                if (phone.getText().toString().length() > 7) {
                    dataManager.setData(DataManager.PHONE, phone.getText().toString());
                    erxesRequest.setConnect("", phone.getText().toString(), false, null);
                    phone.setError(null);
                } else
                    phone.setError(getResources().getString(R.string.no_correct_phone));
            } else {
                if (isValidEmail(email.getText().toString())) {
                    email.setError(null);
                    dataManager.setData(DataManager.EMAIL, email.getText().toString());
                    erxesRequest.setConnect("" + email.getText().toString(), "", false, null);
                } else
                    email.setError(getResources().getString(R.string.no_correct_mail));
            }
        } else {
            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
        config.setActivityConfig(this);
    }

    @Override
    public void notify(final int returnType, String conversationId, final String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case Returntype.LOGINSUCCESS:
                        Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                        a.putExtra("isFromLogin", true);
                        ErxesActivity.this.startActivity(a);
                        ErxesActivity.this.finish();
                        break;

                    case Returntype.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                        break;

                    case Returntype.SERVERERROR:
                        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });


    }

    private View.OnClickListener touchListener = v -> {
        if (v.getId() == R.id.cancelImageView)
            logout();
    };

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
