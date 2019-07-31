package com.newmedia.erxeslibrary.ui.conversations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private Activity activity;
    public List<Conversation> conversationList;
    private Config config;

    public ConversationListAdapter(Activity activity, List<Conversation> conversationList) {
        this.activity = activity;
        config = Config.getInstance(activity);
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chatlist_item, parent, false);
        view.setOnClickListener(onClickListener);
        return new ConversationHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent a = new Intent(activity, MessageActivity.class);
            config.conversationId = conversationList.get((int) view.getTag())._id;
            if (config.conversationMessages != null && config.conversationMessages.size() > 0) {
                config.conversationMessages.clear();
            }
            if (config.conversationMessages != null) {
                config.conversationMessages.addAll(conversationList.get((int) view.getTag()).conversationMessages);
            }
            activity.startActivity(a);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {

        if (conversationList.get(position).isread) {
            if (conversationList.get(position).content != null)
                holder.content.setText(Html.fromHtml(conversationList.get(position).content));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
            holder.name.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
            holder.content.setTextColor(Color.parseColor("#808080"));
        } else {
            holder.content.setText(Html.fromHtml(conversationList.get(position).content));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.BOLD);
            holder.name.setTypeface(holder.content.getTypeface(), Typeface.BOLD);
            holder.content.setTextColor(Color.BLACK);
        }

        ConversationMessage message = null;
        if (conversationList.get(position).conversationMessages.size() > 0) {
            message = conversationList.get(position).conversationMessages
                    .get(conversationList.get(position).conversationMessages.size() - 1);
        }
        if (message != null && message.user != null) {
            String myString = message.user.fullName;
            String upperString = myString.substring(0, 1).toUpperCase() + myString.substring(1);
            holder.name.setText(upperString);
            if (message.user.avatar != null)
                Glide.with(activity).load(message.user.avatar)
                        .placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.circleImageView);
            else
                Glide.with(activity).load(R.drawable.avatar)
                        .placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.circleImageView);

            Long createDate = Long.valueOf(message.createdAt);
            holder.date.setText(config.convert_datetime(createDate));
            holder.parent.setTag(position);
        } else {
            holder.name.setText("Support staff");
            Glide.with(activity).load(R.drawable.avatar)
                    .placeholder(R.drawable.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);
            long createDate = Long.parseLong(conversationList.get(position).date);
//            holder.date.setText(config.convert_datetime(createDate));
            holder.date.setText(config.conversationDate(createDate));
            holder.parent.setTag(position);
        }

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }
}
