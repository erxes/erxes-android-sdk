package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.ui.faq.FaqDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.Holder>  {


    private List<KnowledgeBaseArticle> articles = new ArrayList<>();
    private Activity context;
    private Config config;
    private String categoryId;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent a = new Intent(context, FaqDetailActivity.class);
            a.putExtra("categoryId",categoryId);
            a.putExtra("id",(String) v.getTag());
            context.startActivity(a);
        }
    };
    public ArticleAdapter(Activity context , List<KnowledgeBaseArticle> articles, String categoryId) {
        this.context = context;
        this.config = Config.getInstance(context);
        this.articles = articles;
        this.categoryId = categoryId;
    }

    @NonNull
    @Override
    public ArticleAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.article_item, parent, false);
        view.setOnClickListener(onClickListener);
        return new ArticleAdapter.Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleAdapter.Holder holder, int position) {
        Glide.with(context).load(config.getKnowledgeIcon(context,"clipboard")).into(holder.clipboardImageView);
        holder.title.setText(articles.get(position).title);
        holder.summary.setText(Html.fromHtml(articles.get(position).summary));
        holder.parent.setTag(position);
        holder.parent.setTag(articles.get(position).id);
        holder.parent.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title,summary;
        View parent ;
        ImageView clipboardImageView;

        public Holder(View itemView) {
            super(itemView);
            parent = itemView;
//            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            clipboardImageView = itemView.findViewById(R.id.clipboardImageView);
        }
    }

}
