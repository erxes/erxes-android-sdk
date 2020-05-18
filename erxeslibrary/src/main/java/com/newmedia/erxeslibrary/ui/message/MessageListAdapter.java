package com.newmedia.erxeslibrary.ui.message;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.FileAttachment;
import com.newmedia.erxeslibrary.utils.EnumUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {


    private final int VIEW_SENT = 0;
    private final int VIEW_RECIEVED = 1;
    private final int VIEW_WELCOME = 2;
    private List<ConversationMessage> mMessageList;
    private MessageActivity activity;
    private Config config;

    MessageListAdapter(MessageActivity activity, List<ConversationMessage> mMessageList) {
        this.activity = activity;
        this.config = Config.getInstance(activity);
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_SENT) {
            View view = layoutInflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_RECIEVED) {
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
            return VIEW_WELCOME;

        if (!TextUtils.isEmpty(config.messengerdata.getMessages().getWelcome()))
            mPosition = mPosition - 1;

        if (mMessageList.get(mPosition).user == null)
            return VIEW_SENT;
        else return VIEW_RECIEVED;
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
        LinearLayout textTypeLayout;
        CardView vCallTypeLayout;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            fileRecyclerView = itemView.findViewById(R.id.fileRecyclerView);
            textTypeLayout = itemView.findViewById(R.id.textType);
            vCallTypeLayout = itemView.findViewById(R.id.vCallType);
        }

        void bind(ConversationMessage message) {
            if (message.contentType.equals(EnumUtil.TYPEVCALLREQUEST)) {
                vCallTypeLayout.setVisibility(View.VISIBLE);
                textTypeLayout.setVisibility(View.GONE);
            } else {
                vCallTypeLayout.setVisibility(View.GONE);
                textTypeLayout.setVisibility(View.VISIBLE);

                messageText.setText(config.getHtml(message.content));
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
            timeText.setText(message.createdAt);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        TextView messageText;
        ImageView profileImage;
        RecyclerView fileRecyclerView;
        LinearLayout textTypeLayout;
        CardView vCallTypeLayout;
        CardView vCallTypeEndLayout;
        Button joinVCall;
        TextView orPassToBrowser;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
            profileImage = itemView.findViewById(R.id.image_message_profile);
            fileRecyclerView = itemView.findViewById(R.id.fileRecyclerView);
            textTypeLayout = itemView.findViewById(R.id.textType);
            vCallTypeLayout = itemView.findViewById(R.id.vCallType);
            vCallTypeEndLayout = itemView.findViewById(R.id.vCallTypeEnd);
            joinVCall = itemView.findViewById(R.id.joinVCall);
            orPassToBrowser = itemView.findViewById(R.id.orPassToBrowser);
        }

        void bind(ConversationMessage message) {
            if (message.contentType.equals(EnumUtil.TYPEVCALL)) {
                textTypeLayout.setVisibility(View.GONE);

                if (!message.vCallStatus.equalsIgnoreCase("end")) {
                    vCallTypeLayout.setVisibility(View.VISIBLE);
                    vCallTypeEndLayout.setVisibility(View.GONE);
                    joinVCall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.vCallWebView(message.vCallUrl,message.vCallStatus, message.vCallName);
                        }
                    });
                    String thisLink = "(or click " +"<a href=\"" + message.vCallUrl + "\">" + "this link</a>" + " to open a new tab)";
                    orPassToBrowser.setText(Html.fromHtml(thisLink));
                    orPassToBrowser.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    vCallTypeEndLayout.setVisibility(View.VISIBLE);
                    vCallTypeLayout.setVisibility(View.GONE);
                }

            } else {
                vCallTypeLayout.setVisibility(View.GONE);
                textTypeLayout.setVisibility(View.VISIBLE);

                messageText.setText(config.getHtml(message.content));
                if (message.content.contains("href"))
                    messageText.setMovementMethod(LinkMovementMethod.getInstance());
                else messageText.setMovementMethod(null);

                if (message.user != null) {
                    Glide.with(activity).load(message.user.getAvatar())
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
            timeText.setText(message.createdAt);
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
