package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.ui.faq.FaqActivity;
import com.newmedia.erxeslibrary.ui.faq.FaqDetailActivity;

import io.realm.RealmList;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.Holder>  {


    private RealmList<KnowledgeBaseArticle> articles;
    private Context context;
    private Config config;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent a = new Intent(context, FaqDetailActivity.class);
            a.putExtra("id",(String) v.getTag());
            context.startActivity(a);
        }
    };
    public ArticleAdapter(Context context , RealmList<KnowledgeBaseArticle> articles) {
        this.context = context;
        this.config = Config.getInstance(context);
        this.articles = articles;

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

//        holder.icon.setImageResource(Helper.ICON_MAP.get(categories.get(position).icon).intValue());
        holder.title.setText(articles.get(position).title);
        holder.summary.setText(Html.fromHtml(articles.get(position).summary));
        holder.parent.setTag(position);
        holder.parent.setTag(articles.get(position)._id);
        holder.parent.setOnClickListener(onClickListener);

    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();

        }
    };
    public class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title,summary;
        View parent ;
        public Holder(View itemView) {
            super(itemView);
            parent = itemView;
//            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
        }
    }

}
