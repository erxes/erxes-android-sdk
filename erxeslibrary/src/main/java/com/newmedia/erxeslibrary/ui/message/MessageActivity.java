package com.newmedia.erxeslibrary.ui.message;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.helper.SoftKeyboard;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.utils.EnumUtil;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.ArrayList;
import java.util.List;


public class MessageActivity extends AppCompatActivity implements ErxesObserver {

    private EditText edittextChatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView profile1, profile2, backImageView, logoutImageView, sendImageView, attachmentImageView, vCallImageView;
    private TextView names, isOnline, description;
    private ViewGroup container, uploadGroup, messageContainer, parentLayout, vCallGroup;
    private Config config;
    private ErxesRequest erxesRequest;
    private Point size;
    private GFilePart gFilePart;
    private WebView mWebView;
    private View activeView1, activeView2;

    private MessageListAdapter messageListAdapter;
    private SoftKeyboard softKeyboard;
    private RelativeLayout profile2Layout, profile1Layout;
    private final List<ConversationMessage> conversationMessages = new ArrayList<>();
    private final List<User> participatedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        config = Config.getInstance(this);
        erxesRequest = ErxesRequest.getInstance(config);
        ErxesHelper.changeLanguage(this, config.language);
        config.setActivityConfig(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_messege);

        gFilePart = new GFilePart(config, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);


        container = this.findViewById(R.id.container);
        uploadGroup = this.findViewById(R.id.upload_group);
        messageContainer = this.findViewById(R.id.messageContainer);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profile1 = this.findViewById(R.id.profile1);
        profile2 = this.findViewById(R.id.profile2);
        isOnline = this.findViewById(R.id.isOnline);
        names = this.findViewById(R.id.names);
        edittextChatbox = this.findViewById(R.id.edittext_chatbox);
        mMessageRecycler = this.findViewById(R.id.reyclerview_message_list);
        backImageView = this.findViewById(R.id.backImageView);
        logoutImageView = this.findViewById(R.id.logoutImageView);
        sendImageView = this.findViewById(R.id.sendImageView);
        attachmentImageView = this.findViewById(R.id.attachmentImageView);
        vCallImageView = this.findViewById(R.id.vCallImageView);
        parentLayout = this.findViewById(R.id.linearlayout);
        mWebView = this.findViewById(R.id.webView);
        vCallGroup = this.findViewById(R.id.vCallGroup);
        activeView1 = this.findViewById(R.id.activeView1);
        activeView2 = this.findViewById(R.id.activeView2);
        profile1Layout = this.findViewById(R.id.profile1Layout);
        profile2Layout = this.findViewById(R.id.profile2Layout);
        description = this.findViewById(R.id.description);

        config.setCursorColor(edittextChatbox, config.colorCode);
        if (config.messengerdata.isShowVideoCallRequest()) {
            vCallGroup.setVisibility(View.VISIBLE);
        }

        softKeyboard = new SoftKeyboard(parentLayout, (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                MessageActivity.this.runOnUiThread(() -> {
                    parentLayout.getLayoutParams().height = size.y * 8 / 10;
                    parentLayout.requestLayout();
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                MessageActivity.this.runOnUiThread(() -> {
                    parentLayout.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                    parentLayout.requestLayout();
                });
            }
        });

        size = ErxesHelper.display_configure(this, parentLayout, "#00000000");
        isOnline.setTextColor(config.textColorCode);
        names.setTextColor(config.textColorCode);
        description.setTextColor(config.textColorCode);
        initIcon();

        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);

        swipeRefreshLayout.setOnRefreshListener(this::refreshItems);

        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (config.wallpaper != null) {
            int index;
            try {
                index = Integer.parseInt(config.wallpaper);
            } catch (NumberFormatException e) {
                index = 0;
            }
            if (index != 0 && index < 5) {
                mMessageRecycler.setBackground(getResources().getDrawable(ErxesHelper.backgrounds[index - 1]));
            }
        }

        mMessageRecycler.setLayoutManager(linearLayoutManager);
        messageListAdapter = new MessageListAdapter(this, conversationMessages);
        mMessageRecycler.setAdapter(messageListAdapter);
        if (config.conversationId != null) {
            edittextChatbox.setHint(getResources().getString(R.string.Write_a_reply));
            linearLayoutManager.setStackFromEnd(true);
            erxesRequest.getMessages(config.conversationId);
        }

        header_profile_change();
        if (config.messengerdata.isBotShowInitialMessage()) {
            erxesRequest.getBotInitialMessage();
        }
        erxesRequest.getConversationDetail();

        askPermissions();
    }

    public void vCallWebView(String vCallUrl, String vCallStatus, String vCallName) {
        messageContainer.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);


        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                MessageActivity.this.runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        mWebView.loadUrl(vCallUrl);
    }

    @Override
    public void notify(final int returnType, String conversationId, String message, Object object) {
        this.runOnUiThread(() -> {
            switch (returnType) {
                case ReturntypeUtil.COMINGNEWMESSAGE:
                    if (conversationMessages.size() > 0) {
                        if (((ConversationMessage) object).user != null) {
                            conversationMessages.add(((ConversationMessage) object));
                            if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome())) {
                                messageListAdapter.notifyItemInserted(conversationMessages.size());
                            } else {
                                messageListAdapter.notifyItemInserted(conversationMessages.size() - 1);
                            }
                            mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                    break;
                case ReturntypeUtil.GETMESSAGES:
                    conversationMessages.clear();
                    conversationMessages.addAll((List<ConversationMessage>) object);
                    messageListAdapter.notifyDataSetChanged();
                    if (messageListAdapter.getItemCount() > 0) {
                        mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case ReturntypeUtil.MUTATION:
                    if (object instanceof ConversationMessage) {
                        conversationMessages.add((ConversationMessage) object);
                        if (conversationMessages.size() == 1) {
                            erxesRequest.getConversationDetail();
                            config.intent.putExtra("id", config.conversationId);
                            startService(config.intent);
                        }
                        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome())) {
                            messageListAdapter.notifyItemInserted(conversationMessages.size());
                        } else {
                            messageListAdapter.notifyItemInserted(conversationMessages.size() - 1);
                        }

                        mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                        swipeRefreshLayout.setRefreshing(false);

                        gFilePart.end_of();
                        edittextChatbox.setText("");
                        edittextChatbox.setHint(getResources().getString(R.string.Write_a_reply));
                    }
                    break;
                case ReturntypeUtil.SERVERERROR:
                    Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case ReturntypeUtil.CONNECTIONFAILED:
                    Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case ReturntypeUtil.GETCONVERSATIONDETAIL:
                    participatedUsers.clear();
                    participatedUsers.addAll(((List<User>) object));

                    header_profile_change();
                    break;
                case ReturntypeUtil.GETSUPPORTERS:
                    if (participatedUsers.size() == 0) {
                        header_profile_change();
                    }

                    break;
                case ReturntypeUtil.GETBOTINITIALMESSAGE:
                    if (object != null) {
//                        config.conversationId = String.valueOf(object);
//                        erxesRequest.changeOperator(config.conversationId);
                    }
                    break;
            }
        });
    }


    private void bind(User user, ImageView por) {
        por.setVisibility(View.VISIBLE);
        if (user.getAvatar() != null) {
            Glide.with(MessageActivity.this)
                    .load(user.getAvatar())
                    .placeholder(R.drawable.avatar)
                    .optionalCircleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(por);

        } else {
            Glide.with(MessageActivity.this)
                    .load(R.drawable.avatar)
                    .optionalCircleCrop()
                    .into(por);
        }
    }

    private void header_profile_change() {
        Drawable drawable = getDrawable(R.drawable.circle_inactive);
        drawable.setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);
        profile1.setBackground(drawable);
        profile2.setBackground(drawable);
        Drawable activeDrawable = getDrawable(R.drawable.circle_active);
        Drawable inactiveDrawable = getDrawable(R.drawable.circle_inactive);
        activeView1.setBackground(config.isOnline ? activeDrawable : inactiveDrawable);
        activeView2.setBackground(config.isOnline ? activeDrawable : inactiveDrawable);


        if (participatedUsers.size() > 0) {
            bind(participatedUsers.get(0), profile1);
            profile2Layout.setVisibility(View.GONE);

            String name;
            if (!TextUtils.isEmpty(participatedUsers.get(0).getFullName())) {
                name = participatedUsers.get(0).getFullName();
            } else {
                name = participatedUsers.get(0).getShortName();
            }
            names.setText(name);
            isOnline.setText(participatedUsers.get(0).getPosition());
            if (!TextUtils.isEmpty(participatedUsers.get(0).getDescription())) {
                description.setVisibility(View.VISIBLE);
                description.setText(participatedUsers.get(0).getDescription());
            }
        } else {
            description.setVisibility(View.GONE);
            if (config.supporters.size() > 0) bind(config.supporters.get(0), profile1);
            else profile1Layout.setVisibility(View.GONE);
            if (config.supporters.size() > 1) bind(config.supporters.get(1), profile2);
            else profile2Layout.setVisibility(View.GONE);
            names.setText(config.brandName);
            isOnline.setText(config.brandDescription);
        }

    }

    private void initIcon() {
        Glide.with(this).load(config.getBackIcon(this, config.textColorCode)).into(backImageView);
        Glide.with(this).load(config.getLogoutIcon(this, config.textColorCode)).into(logoutImageView);
        Glide.with(this).load(config.getsendIcon(this, 0)).into(sendImageView);
        Glide.with(this).load(config.getAttachmentIcon(this, 0)).into(attachmentImageView);
        Glide.with(this).load(config.getVCallIcon(this, 0)).into(vCallImageView);
    }

    public void logout() {
        config.Logout(this);
    }

    public void send_message(View view) {
        if (!config.isNetworkConnected()) {
            Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String mContent = edittextChatbox.getText().toString();
        if (!mContent.equalsIgnoreCase("") ||
                gFilePart.get() != null && gFilePart.get().size() > 0) {
            erxesRequest.InsertMessage(mContent, gFilePart.get(), EnumUtil.TYPETEXT);
        }
    }

    public void refreshItems() {
        if (config.conversationId != null)
            erxesRequest.getMessages(config.conversationId);
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        erxesRequest.remove(this);
        if (softKeyboard != null) {
            softKeyboard.closeSoftKeyboard();
            softKeyboard.unRegisterSoftKeyboardCallback();
        }
        if (config.intent != null)
            stopService(config.intent);
        participatedUsers.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
    }

    public void onVCall(View view) {
        erxesRequest.InsertMessage(null, new ArrayList<>(), EnumUtil.TYPEVCALLREQUEST);
    }

    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_PICK);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 444);
        uploadGroup.setClickable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        gFilePart.ActivityResult(requestCode, resultCode, resultData);
        uploadGroup.setClickable(true);
    }

    protected void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.CAMERA",
                    "android.permission.MODIFY_AUDIO_SETTINGS"
            };
            int requestCode = 200;

            requestPermissions(permissions, requestCode);
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
