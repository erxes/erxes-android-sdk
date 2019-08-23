package com.newmedia.erxeslibrary.ui.faq;

import android.app.Service;
import android.graphics.Point;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.helper.SoftKeyboard;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.ui.conversations.adapter.ArticleAdapter;

public class FaqActivity extends AppCompatActivity {
    private ViewGroup container;
    private Point size;
    private Config config;
    private ImageView backImageView, cancelImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_faq);
        config = Config.getInstance(this);
        config.setActivityConfig(this);
        load_findViewByid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        config.setActivityConfig(this);
        if(config.customerId == null) {
            this.finish();
        }
    }

    private void load_findViewByid(){
        container = this.findViewById(R.id.container);
        backImageView = this.findViewById(R.id.backImageView);
        cancelImageView = this.findViewById(R.id.cancelImageView);
        initIcon();

        size = ErxesHelper.display_configure(this,container,"#00000000");
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard((ViewGroup)this.findViewById(R.id.linearlayout), im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height =size.y*8/10;
                        container.requestLayout();
                    }
                });
            }
            @Override
            public void onSoftKeyboardShow() {
                FaqActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });
            }
        });
        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        this.findViewById(R.id.cancelImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        this.findViewById(R.id.backImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        this.findViewById(R.id.cancelImageView).setOnClickListener(v -> logout());
        this.findViewById(R.id.backImageView).setOnClickListener(v -> finish());
        RecyclerView recyclerView = this.findViewById(R.id.recycler_view);
        TextView general = this.findViewById(R.id.general);
        general.setTextColor(config.getInColor(config.colorCode));
        TextView generalNumber = this.findViewById(R.id.general_number);
        generalNumber.setTextColor(config.getInColorGray(config.colorCode));
        TextView generalDescription = this.findViewById(R.id.general_description);
        generalDescription.setTextColor(config.getInColorGray(config.colorCode));
        String id = getIntent().getStringExtra("id");
        if( id != null) {
            KnowledgeBaseCategory knowledgeBaseCategory = null;
            String categoryId = null;
            for (int i = 0; i < config.knowledgeBaseTopic.categories.size(); i ++) {
                if (config.knowledgeBaseTopic.categories.get(i).id.equals(id)) {
                    knowledgeBaseCategory = config.knowledgeBaseTopic.categories.get(i);
                    categoryId = knowledgeBaseCategory.id;
                    break;
                }
            }
            general.setText(knowledgeBaseCategory.title);
            generalNumber.setText("("+knowledgeBaseCategory.numOfArticles+")");
            generalDescription.setText(knowledgeBaseCategory.description);
            recyclerView.setAdapter(new ArticleAdapter(this, knowledgeBaseCategory.articles,categoryId));
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void initIcon() {
        Glide.with(this).load(config.getBackIcon(this,config.getInColor(config.colorCode))).into(backImageView);
        Glide.with(this).load(config.getLogoutIcon(this,config.getInColor(config.colorCode))).into(cancelImageView);
    }

    public void logout(){
        config.Logout(this);
    }
}
