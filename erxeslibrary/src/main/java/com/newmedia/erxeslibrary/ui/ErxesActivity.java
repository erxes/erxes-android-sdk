package com.newmedia.erxeslibrary.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.utils.DataManager;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.R;

public class ErxesActivity extends AppCompatActivity implements ErxesObserver/*, ProviderInstaller.ProviderInstallListener*/  {

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
    private String customData, mEmail, mPhone;

    private static final int ERROR_DIALOG_REQUEST_CODE = 1;

    private boolean retryProviderInstall, hasData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        contact.setTextColor(config.getInColor(config.colorCode));

        ErxesHelper.display_configure(this, container, "#66000000");
        change_color();
        cancelImageView.setOnClickListener(touchListener);
        initIcon();

        boolean isProvider = getIntent().getBooleanExtra("isProvider", false);
        hasData = getIntent().getBooleanExtra("hasData", false);
        customData = getIntent().getStringExtra("customData");
        mEmail = getIntent().getStringExtra("mEmail");
        mPhone = getIntent().getStringExtra("mPhone");
//        if (isProvider) {
//            ProviderInstaller.installIfNeededAsync(this, this);
//        } else {
            init();
//        }
    }

    private void init() {
        if (hasData) {
            erxesRequest.setConnect(false,false, true, true, mEmail, mPhone, customData);
        } else if (config.isLoggedIn()) {
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

//    @Override
//    public void onProviderInstalled() {
//        Log.e("TAG", "onProviderInstalled: " );
//        dataManager.setData(DataManager.HASPROVIDER,true);
//        if (hasData)
//            erxesRequest.setConnect(true, true, true, hasData, mEmail, mPhone, customData);
//        else erxesRequest.setConnect(true, true, false, hasData, mEmail, mPhone, customData);
//    }
//
//    @Override
//    public void onProviderInstallFailed(int errorCode, Intent intent) {
//        Log.e("TAG", "onProviderInstallFailed: " );
//        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
//        if (availability.isUserResolvableError(errorCode)) {
//            // Recoverable error. Show a dialog prompting the user to
//            // install/update/enable Google Play services.
//            availability.showErrorDialogFragment(
//                    this,
//                    errorCode,
//                    ERROR_DIALOG_REQUEST_CODE,
//                    new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            // The user chose not to take the recovery action
//
//                            onProviderInstallerNotAvailable();
//                        }
//                    });
//        } else {
//            // Google Play services is not available.
//            onProviderInstallerNotAvailable();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,
//                                    Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
//            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
//            // before the instance state is restored throws an error. So instead,
//            // set a flag here, which will cause the fragment to delay until
//            // onPostResume.
//            retryProviderInstall = true;
//        }
//    }

    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
//        if (retryProviderInstall) {
//            // We can now safely retry installation.
//            ProviderInstaller.installIfNeededAsync(this, this);
//        }
//        retryProviderInstall = false;
    }

    private void onProviderInstallerNotAvailable() {
        Log.e("TAG", "onProviderInstallerNotAvailable: " );
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        finish();
    }

    private void initIcon() {
        Glide.with(this).load(config.getsendIcon(this, 0)).into(sendImageView);
        Glide.with(this).load(config.getCancelIcon(config.getInColor(config.colorCode))).into(cancelImageView);
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
        Drawable drawable = this.findViewById(R.id.selector).getBackground();
        drawable.setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
        email_click(null);
    }

    public void email_click(View v) {
        email.setVisibility(View.VISIBLE);
        phone.setVisibility(View.GONE);

        mailCardView.setCardBackgroundColor(config.colorCode);
        smsCardView.setCardBackgroundColor(getResources().getColor(R.color.md_white_1000));
        emailButton.setTextColor(config.getInColor(config.colorCode));
        smsButton.setTextColor(config.getInColorGray(getResources().getColor(R.color.md_white_1000)));
        changeEmailColor(config.getInColor(config.colorCode));
        changePhoneColor(config.getInColorGray(getResources().getColor(R.color.md_white_1000)));
    }

    public void sms_click(View v) {
        email.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);

        smsCardView.setCardBackgroundColor(config.colorCode);
        mailCardView.setCardBackgroundColor(getResources().getColor(R.color.md_white_1000));
        smsButton.setTextColor(config.getInColor(config.colorCode));
        emailButton.setTextColor(config.getInColorGray(getResources().getColor(R.color.md_white_1000)));
        changePhoneColor(config.getInColor(config.colorCode));
        changeEmailColor(config.getInColorGray(getResources().getColor(R.color.md_white_1000)));
    }

    public void logout() {
        this.finish();
    }

    public void Connect_click(View v) {
        if (config.isNetworkConnected()) {
            if (email.getVisibility() == View.GONE) {
                if (phone.getText().toString().length() > 7) {
                    dataManager.setData(DataManager.PHONE, phone.getText().toString());
                    erxesRequest.setConnect(false,false, false, false, "", phone.getText().toString(), null);
                    phone.setError(null);
                } else
                    phone.setError(getResources().getString(R.string.Failed));
            } else {
                if (isValidEmail(email.getText().toString())) {
                    email.setError(null);
                    dataManager.setData(DataManager.EMAIL, email.getText().toString());
                    erxesRequest.setConnect(false,false, false, false, email.getText().toString(), "", null);
                } else
                    email.setError(getResources().getString(R.string.Failed));
            }
        } else {
            Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
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
                    case ReturntypeUtil.LOGINSUCCESS:
                        Intent a = new Intent(ErxesActivity.this, ConversationListActivity.class);
                        a.putExtra("isFromLogin", true);
                        ErxesActivity.this.startActivity(a);
                        ErxesActivity.this.finish();
                        break;

                    case ReturntypeUtil.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                        break;

                    case ReturntypeUtil.SERVERERROR:
                        Snackbar.make(container, message, Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });


    }

    private View.OnClickListener touchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cancelImageView)
                logout();
        }
    };

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


}
