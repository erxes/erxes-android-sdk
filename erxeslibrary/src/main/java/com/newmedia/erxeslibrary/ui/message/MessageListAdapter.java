package com.newmedia.erxeslibrary.ui.message;

import android.app.Activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.*;
import com.newmedia.erxeslibrary.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {


    private List<ConversationMessage> mMessageList;
    private Activity activity;
    private int previousSize = 0;
    private Config config;

    MessageListAdapter(Activity activity, List<ConversationMessage> mMessageList) {
        this.activity = activity;
        this.config = Config.getInstance(activity);
        this.mMessageList = mMessageList;
        this.previousSize = this.mMessageList.size();
    }

    boolean RefreshData() {
        if (mMessageList.size() > previousSize) {
            int counterBefore = mMessageList.size();
            int zoruu = mMessageList.size() - previousSize;

            previousSize = mMessageList.size();
            if (config.messengerdata.getMessages().getWelcome() != null) {
                if (zoruu == 1)
                    notifyItemInserted(mMessageList.size());
                else
                    notifyItemRangeInserted(counterBefore + 1, zoruu);
            } else {
                if (zoruu == 1)
                    notifyItemInserted(mMessageList.size() - 1);
                else
                    notifyItemRangeInserted(counterBefore, zoruu);

            }
            return true;
        } else
            return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == 0) {
            View view = layoutInflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == 1) {
            View view = layoutInflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.item_message_welcome, parent, false);
            return new WelcomeMessageHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        int mPosition = position;
        if (mPosition == 0 && !TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()))
            return 2; //welcomeMessage

        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()))
            mPosition = mPosition - 1;

        if (config.customerId.equalsIgnoreCase(mMessageList.get(mPosition).customerId))
            return 0;
        else return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConversationMessage message;
        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()) && (position == 0)) {
            message = new ConversationMessage();
            message.content = (config.messengerdata.getMessages().getWelcome());
            message.createdAt = ("");
        } else if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()))
            message = mMessageList.get(position - 1);
        else
            message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case 0:
                ((SentMessageHolder) holder).bind(message);
                break;
            case 1:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
            case 2:
                ((WelcomeMessageHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()))
            return mMessageList.size() + 1;
        else
            return mMessageList.size();
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        RecyclerView fileRecyclerView;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            fileRecyclerView = itemView.findViewById(R.id.fileRecyclerView);
        }

        void bind(ConversationMessage message) {
            messageText.setText(config.getHtml(message.content));
//            timeText.setText(config.MessageDatetime(message.createdAt));
            timeText.setText(message.createdAt);
            Log.e("TAG", "bind: " + message.attachments.size() );
            if (message.attachments != null) {
                if (message.attachments.size() > 0) {
                    List<FileAttachment> fileAttachmentList = new ArrayList<>();
                    for (int i = 0; i < message.attachments.size(); i++) {
                        FileAttachment fileAttachment = new FileAttachment();
                        fileAttachment.setName(message.attachments.get(i).getName());
                        fileAttachment.setSize(message.attachments.get(i).getSize());
                        fileAttachment.setType(message.attachments.get(i).getType());
                        fileAttachment.setUrl(message.attachments.get(i).getUrl());
                        fileAttachmentList.add(fileAttachment);
                    }
                    GridLayoutManager gridLayoutManager;
                    if (fileAttachmentList.size() > 2) {
                        gridLayoutManager = new GridLayoutManager(activity, 3);
                    } else {
                        gridLayoutManager = new GridLayoutManager(activity, fileAttachmentList.size());
                    }
                    fileRecyclerView.setVisibility(View.VISIBLE);
                    fileRecyclerView.setLayoutManager(gridLayoutManager);
                    fileRecyclerView.setHasFixedSize(true);
                    fileRecyclerView.setAdapter(new FileAdapter(activity, fileAttachmentList));
                } else {
                    fileRecyclerView.setVisibility(View.GONE);
                }
            } else {
                fileRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        TextView messageText;
        ImageView profileImage;
        RecyclerView fileRecyclerView;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            profileImage = itemView.findViewById(R.id.image_message_profile);
            fileRecyclerView = itemView.findViewById(R.id.fileRecyclerView);
        }

        void bind(ConversationMessage message) {
            messageText.setText(config.getHtml(message.content));
            if (message.content.contains("href"))
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
            else messageText.setMovementMethod(null);

//            timeText.setText(config.MessageDatetime(message.createdAt));
            timeText.setText(message.createdAt);

            if (message.user != null) {
                Glide.with(activity).load(message.user.avatar)
                        .placeholder(R.drawable.avatar)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            } else
                Glide.with(activity).load(R.drawable.avatar)
                        .placeholder(R.drawable.avatar)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);

            if (message.attachments != null) {
                if (message.attachments.size() > 0) {
                    List<FileAttachment> fileAttachmentList = new ArrayList<>();
                    for (int i = 0; i < message.attachments.size(); i++) {
                        FileAttachment fileAttachment = new FileAttachment();
                        fileAttachment.setName(message.attachments.get(i).getName());
                        fileAttachment.setSize(message.attachments.get(i).getSize());
                        fileAttachment.setType(message.attachments.get(i).getType());
                        fileAttachment.setUrl(message.attachments.get(i).getUrl());
                        fileAttachmentList.add(fileAttachment);
                    }
                    GridLayoutManager gridLayoutManager;
                    if (fileAttachmentList.size() > 2) {
                        gridLayoutManager = new GridLayoutManager(activity, 3);
                    } else {
                        gridLayoutManager = new GridLayoutManager(activity, fileAttachmentList.size());
                    }
                    fileRecyclerView.setVisibility(View.VISIBLE);
                    fileRecyclerView.setLayoutManager(gridLayoutManager);
                    fileRecyclerView.setHasFixedSize(true);
                    fileRecyclerView.setAdapter(new FileAdapter(activity, fileAttachmentList));
                } else {
                    fileRecyclerView.setVisibility(View.GONE);
                }
            } else {
                fileRecyclerView.setVisibility(View.GONE);
            }

        }
    }

    private class WelcomeMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        WelcomeMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.text_message_body);
        }

        void bind(ConversationMessage message) {
            messageText.setText(config.getHtml(message.content));
        }
    }
}
