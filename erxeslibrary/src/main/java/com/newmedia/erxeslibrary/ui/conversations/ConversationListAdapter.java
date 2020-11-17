package com.newmedia.erxeslibrary.ui.conversations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.ui.message.MessageActivity;

import java.util.List;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private final Context context;
    public List<Conversation> conversationList;
    private final Config config;

    public ConversationListAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        config = Config.getInstance(context);
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chatlist_item, parent, false);
        return new ConversationHolder(view);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent a = new Intent(context, MessageActivity.class);
            config.conversationId = conversationList.get((int) view.getTag()).id;
            context.startActivity(a);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {

        holder.itemClick.setOnClickListener(onClickListener);

        if (conversationList.get(position).isread) {
            if (!TextUtils.isEmpty(conversationList.get(position).content)) {
                holder.content.setText(config.getHtml(conversationList.get(position).content));
            }
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
            holder.content.setTextColor(Color.parseColor("#808080"));
        } else {
            if (!TextUtils.isEmpty(conversationList.get(position).content)) {
                holder.content.setText(config.getHtml(conversationList.get(position).content));
            }
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.BOLD);
            holder.content.setTextColor(Color.BLACK);
        }
        if (conversationList.get(position).participatedUsers.size() > 0) {

            holder.name.setText(conversationList.get(position).participatedUsers.get(0).getFullName());

            if (conversationList.get(position).participatedUsers.get(0).getAvatar() != null)
                Glide.with(context).load(conversationList.get(position).participatedUsers.get(0).getAvatar())
                        .placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .circleCrop()
                        .into(holder.circleImageView);
            else
                Glide.with(context).load(R.drawable.avatar)
                        .placeholder(R.drawable.avatar)
                        .into(holder.circleImageView);

            holder.date.setText(conversationList.get(position).date);
            holder.parent.setTag(position);
        } else {
            holder.name.setText(ErxesHelper.getLocalizedResources(context,config.language).getString(R.string.Support_staff));
            Glide.with(context).load(R.drawable.avatar)
                    .placeholder(R.drawable.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);
            holder.date.setText(conversationList.get(position).date);
            holder.parent.setTag(position);
        }

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }
}
