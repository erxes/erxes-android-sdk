package com.newmedia.erxeslibrary.ui.login;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.LayoutInflaterCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
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
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.R;

public class ErxesActivity extends AppCompatActivity implements ErxesObserver {

    private EditText email, phone;
    private TextView sms_button, email_button, contact;
    private LinearLayout container;
    private ImageView mailzurag, phonezurag, sendImageView, cancelImageView;
    private CardView mailgroup, smsgroup;
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
        setContentView(R.layout.activity_erxes);

        loaderView = this.findViewById(R.id.loaderView);
        email = this.findViewById(R.id.email);
        phone = this.findViewById(R.id.phone);
        container = findViewById(R.id.linearlayout);
        sms_button = this.findViewById(R.id.sms_button);
        email_button = this.findViewById(R.id.email_button);
        mailgroup = this.findViewById(R.id.mailgroup);
        smsgroup = this.findViewById(R.id.smsgroup);
        mailzurag = this.findViewById(R.id.mail_zurag);
        phonezurag = this.findViewById(R.id.phonezurag);
        sendImageView = this.findViewById(R.id.sendImageView);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        contact = this.findViewById(R.id.contact);

        Helper.display_configure(this, container, "#66000000");
        cancelImageView.setOnClickListener(touchListener);
        change_color();
        initIcon();

        boolean hasData = getIntent().getBooleanExtra("hasData",false);
        String customData = getIntent().getStringExtra("customData");
        String mEmail = getIntent().getStringExtra("mEmail");
        String mPhone = getIntent().getStringExtra("mPhone");
        if (hasData) {
            erxesRequest.setConnect(mEmail, mPhone, true, customData);
        } else {
            if (config.isLoggedIn()) {
                config.LoadDefaultValues();
                Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                a.putExtra("isFromLogin",false);
                ErxesActivity.this.startActivity(a);
                finish();
            } else {
                erxesRequest.getIntegration();
            }
        }
    }

    private void initIcon() {
        Glide.with(this).load(config.getsendIcon(this,-1)).into(sendImageView);
        Glide.with(this).load(config.getCancelIcon(this,R.color.md_white_1000)).into(cancelImageView);
        Glide.with(this).load(config.getEmailIcon(this,R.color.md_white_1000)).into(mailzurag);
        Glide.with(this).load(config.getPhoneIcon(this,R.color.md_white_1000)).into(phonezurag);
    }

    private void change_color() {
        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        mailgroup.setCardBackgroundColor(config.colorCode);
        sms_button.setTextColor(config.colorCode);
        changeBitmapColor(phonezurag, config.colorCode);

        Drawable drawable = this.findViewById(R.id.selector).getBackground();
        drawable.setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);

    }

    public void email_click(View v) {
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.GONE);
        /////
        smsgroup.setCardBackgroundColor(Color.WHITE);
        email_button.setTextColor(Color.WHITE);
        changeBitmapColor(mailzurag, Color.WHITE);
        sms_button.setTextColor(config.colorCode);
        ((CardView) v).setCardBackgroundColor(config.colorCode);
        changeBitmapColor(phonezurag, config.colorCode);

    }

    public void changeBitmapColor(ImageView image, int color) {
        image.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void sms_click(View v) {

        email.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);

        mailgroup.setCardBackgroundColor(Color.WHITE);
        sms_button.setTextColor(Color.WHITE);
        changeBitmapColor(phonezurag, Color.WHITE);

        email_button.setTextColor(config.colorCode);
        ((CardView) v).setCardBackgroundColor(config.colorCode);
        changeBitmapColor(mailzurag, config.colorCode);

    }

    public void logout() {
        this.finish();
    }

    public void Connect_click(View v) {
        if (config.isNetworkConnected()) {
            if (email.getVisibility() == View.GONE) {
                if (phone.getText().toString().length() > 7) {
                    dataManager.setData(DataManager.phone, phone.getText().toString());
                    erxesRequest.setConnect("", phone.getText().toString(), false,null);
                    phone.setError(null);
                } else
                    phone.setError(getResources().getString(R.string.no_correct_phone));
            } else {
                if (isValidEmail(email.getText().toString())) {
                    email.setError(null);
                    dataManager.setData(DataManager.email, email.getText().toString());
                    erxesRequest.setConnect("" + email.getText().toString(), "", false,null);
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
    }

    @Override
    public void notify(final int returnType, String conversationId, final String message) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturnType.LOGIN_SUCCESS:
                        Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                        a.putExtra("isFromLogin",true);
                        ErxesActivity.this.startActivity(a);
                        ErxesActivity.this.finish();
                        break;

                    case ReturnType.INTEGRATION_CHANGED:
                        change_color();
                        contact.setVisibility(View.VISIBLE);
                        loaderView.setVisibility(View.GONE);
                        break;

                    case ReturnType.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                        break;

                    case ReturnType.SERVERERROR:
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
