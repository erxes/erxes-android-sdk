package com.newmedia.erxeslibrary.ui.message;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.helper.SoftKeyboard;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.utils.EnumUtil;
import com.newmedia.erxeslibrary.utils.ErxesObserver;
import com.newmedia.erxeslibrary.utils.ReturntypeUtil;

import java.util.ArrayList;


public class MessageActivity extends AppCompatActivity implements ErxesObserver {

    private EditText edittextChatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView profile1, profile2, backImageView, logoutImageView, sendImageView, attachmentImageView, vCallImageView;
    private TextView names, isOnline;
    private ViewGroup container, uploadGroup, messageContainer, parentLayout;
    private Config config;
    private ErxesRequest erxesRequest;
    private Point size;
    private GFilePart gFilePart;
    private WebView mWebView;

    private MessageListAdapter messageListAdapter;
    private SoftKeyboard softKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.getInstance(this);
        config.setActivityConfig(this);
        erxesRequest = ErxesRequest.getInstance(config);
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

        config.setCursorColor(edittextChatbox,config.colorCode);

        softKeyboard = new SoftKeyboard(parentLayout, (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE));
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parentLayout.getLayoutParams().height = size.y * 8 / 10;
                        parentLayout.requestLayout();
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parentLayout.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        parentLayout.requestLayout();
                    }
                });
            }
        });

        size = ErxesHelper.display_configure(this, parentLayout, "#00000000");
        isOnline.setTextColor(config.getInColorGray(config.colorCode));
        names.setTextColor(config.getInColor(config.colorCode));
        initIcon();

        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }

        });

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

        int index = Integer.getInteger(config.wallpaper, -1);
        if (index > -1 && index < 5)
            mMessageRecycler.setBackgroundResource(ErxesHelper.backgrounds[index]);

        mMessageRecycler.setLayoutManager(linearLayoutManager);
        messageListAdapter = new MessageListAdapter(this, config.conversationMessages);
        mMessageRecycler.setAdapter(messageListAdapter);
        if (config.conversationId != null) {
            edittextChatbox.setHint(getResources().getString(R.string.Write_a_reply));
            linearLayoutManager.setStackFromEnd(true);
            for (int i = 0; i < config.conversations.size(); i++) {
                if (config.conversations.get(i).id.equals(config.conversationId)) {
                    config.conversations.get(i).isread = true;
                    break;
                }
            }
            erxesRequest.getMessages(config.conversationId);
        } else {
            config.conversationMessages.clear();
        }

        header_profile_change();

        askPermissions();
    }

    public void vCallWebView(String vCallUrl, String vCallStatus, String vCallName) {
        messageContainer.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl(vCallUrl);
    }

    @Override
    public void notify(final int returnType, String conversationId, String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (returnType) {
                    case ReturntypeUtil.COMINGNEWMESSAGE:
                        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome())) {
                            messageListAdapter.notifyItemInserted(config.conversationMessages.size());
                        } else {
                            messageListAdapter.notifyItemInserted(config.conversationMessages.size() - 1);
                        }
                        mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturntypeUtil.GETMESSAGES:
                        messageListAdapter.notifyDataSetChanged();
                        mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturntypeUtil.MUTATION:
                        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome())) {
                            messageListAdapter.notifyItemInserted(config.conversationMessages.size());
                        } else {
                            messageListAdapter.notifyItemInserted(config.conversationMessages.size() - 1);
                        }

                        mMessageRecycler.smoothScrollToPosition(messageListAdapter.getItemCount() - 1);
                        swipeRefreshLayout.setRefreshing(false);

                        gFilePart.end_of();
                        edittextChatbox.setText("");
                        edittextChatbox.setHint(getResources().getString(R.string.Write_a_reply));
                        break;
                    case ReturntypeUtil.SERVERERROR:
                        Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturntypeUtil.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.Failed, Snackbar.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturntypeUtil.GETSUPPORTERS:
                        header_profile_change();
                        break;
                }
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

        String t = user.getFullName();
        String upperString = t.substring(0, 1).toUpperCase() + t.substring(1);
        String previous = names.getText().toString();
        names.setText(previous.length() == 0 ? upperString : previous + "," + upperString);
        names.setVisibility(View.VISIBLE);
    }

    private void header_profile_change() {
        if (config.supporters.size() > 0)
            isOnline.setVisibility(View.VISIBLE);
        else
            names.setVisibility(View.INVISIBLE);

        names.setText("");

        if (config.supporters.size() > 0) bind(config.supporters.get(0), profile1);
        else profile1.setVisibility(View.INVISIBLE);
        if (config.supporters.size() > 1) bind(config.supporters.get(1), profile2);
        else profile2.setVisibility(View.INVISIBLE);

        isOnline.setText(config.messengerdata.isOnline() ? R.string.Online : R.string.Offline);

    }

    private void initIcon() {
        Glide.with(this).load(config.getBackIcon(this, config.getInColor(config.colorCode))).into(backImageView);
        Glide.with(this).load(config.getLogoutIcon(this, config.getInColor(config.colorCode))).into(logoutImageView);
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
            erxesRequest.InsertMessage(mContent, config.conversationId, gFilePart.get(), EnumUtil.TYPETEXT);
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
        softKeyboard.closeSoftKeyboard();
        softKeyboard.unRegisterSoftKeyboardCallback();
        if (config.intent != null)
            stopService(config.intent);
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
        erxesRequest.getSupporters();
    }

    public void onVCall(View view) {
        erxesRequest.InsertMessage(null, config.conversationId, new ArrayList<>(), EnumUtil.TYPEVCALLREQUEST);
    }

    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_PICK);
//        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 444);
        uploadGroup.setClickable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        gFilePart.ActivityResult(requestCode, resultCode, resultData);
        uploadGroup.setClickable(true);
    }

    protected void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.RECORD_AUDIO",
                    "android.permission.CAMERA"
            };
            int requestCode = 200;

            requestPermissions(permissions, requestCode);
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
