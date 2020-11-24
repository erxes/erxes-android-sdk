package com.newmedia.erxeslibrary.ui.faq;

import android.app.Service;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.helper.SoftKeyboard;
import com.newmedia.erxeslibrary.model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;

public class FaqDetailActivity extends AppCompatActivity {
    private ViewGroup container;
    private Point size;
    private Config config;
    private ImageView backImageView, cancelImageView;
    private View loaderView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        config = Config.getInstance(this);
        ErxesHelper.changeLanguage(this, config.language);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_faq_detail);

        load_findViewByid();
    }
    private void load_findViewByid(){
        container = this.findViewById(R.id.container);
        backImageView = this.findViewById(R.id.backImageView);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        loaderView = this.findViewById(R.id.loaderView);
        initIcon();

        size = ErxesHelper.display_configure(this,container,"#00000000");
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard(this.findViewById(R.id.linearlayout), im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                FaqDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height =size.y*8/10;
                        container.requestLayout();
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                FaqDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });
            }
        });
        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        cancelImageView.setOnClickListener(new View.OnClickListener() {
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

        TextView general = this.findViewById(R.id.general);
        general.setTextColor(config.textColorCode);
        TextView articleHeader = this.findViewById(R.id.article_header);
        TextView date = this.findViewById(R.id.article_date);
        TextView content1 = this.findViewById(R.id.article_content1);
        webView = this.findViewById(R.id.article_content2);

        String id = getIntent().getStringExtra("id");
        String categoryId = getIntent().getStringExtra("categoryId");

        if(id!=null) {
            KnowledgeBaseCategory knowledgeBaseCategory = null;
            for (int i = 0; i < config.knowledgeBaseTopic.categories.size(); i ++) {
                if (config.knowledgeBaseTopic.categories.get(i).id.equals(categoryId)) {
                    knowledgeBaseCategory = config.knowledgeBaseTopic.categories.get(i);
                    break;
                }
            }
            if (knowledgeBaseCategory != null) {
                KnowledgeBaseArticle knowledgeBaseArticle = null;
                for (int i = 0 ; i < knowledgeBaseCategory.articles.size(); i ++) {
                    if (knowledgeBaseCategory.articles.get(i).id.equals(id)) {
                        knowledgeBaseArticle = knowledgeBaseCategory.articles.get(i);
                        break;
                    }
                }
                if (knowledgeBaseArticle != null) {
                    general.setText(knowledgeBaseArticle.title);
                    date.setText("Created : " + config.FullDate(knowledgeBaseArticle.createdDate));
                    articleHeader.setText(knowledgeBaseArticle.title);
                    content1.setText(Html.fromHtml(knowledgeBaseArticle.summary));
                    content1.setMovementMethod(LinkMovementMethod.getInstance());
                    webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
                    webView.getSettings().setLoadWithOverviewMode(true);
                    webView.getSettings().setUseWideViewPort(true);

                    webView.getSettings().setBuiltInZoomControls(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.getSettings().setLoadsImagesAutomatically(true);
                    webView.clearCache(false);
                    webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            loaderView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                            Log.e("TAG", "onReceivedError: " + error.toString() );
                        }

                    });

                    String newhtml_code = Base64.encodeToString(knowledgeBaseArticle.content.getBytes(), Base64.NO_PADDING);

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadData(newhtml_code, "text/html", "base64");

                }
            }
        }
    }

    private void initIcon() {
        Glide.with(this).load(config.getBackIcon(this,config.textColorCode)).into(backImageView);
        Glide.with(this).load(config.getLogoutIcon(this,config.textColorCode)).into(cancelImageView);
    }

    public void logout(){
        config.Logout(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        config.setActivityConfig(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
