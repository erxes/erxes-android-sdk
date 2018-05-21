package com.newmedia.erxeslibrary;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {


    private List<ConversationMessage> mMessageList;
    private int previous_size = 0;
    public MessageListAdapter(List<ConversationMessage> mMessageList) {

        this.mMessageList =  mMessageList;
        this.previous_size = this.mMessageList.size();
    }

    public void setmMessageList(List<ConversationMessage> mMessageList) {
        this.mMessageList = mMessageList;
    }

    public boolean refresh_data(){
        Log.d("erxes_api","sizes ?" +this.mMessageList.size()+" : "+this.previous_size);
        if(mMessageList.size() > previous_size) {
            int counter_before = mMessageList.size();
            int zoruu = mMessageList.size() - previous_size;

            previous_size = mMessageList.size();
            if(Config.welcomeMessage!=null) {
                if (zoruu == 1)
                    notifyItemInserted(mMessageList.size());
                else
                    notifyItemRangeInserted(counter_before+1, zoruu);
            }else{
                if (zoruu == 1)
                    notifyItemInserted(mMessageList.size() - 1);
                else
                    notifyItemRangeInserted(counter_before, zoruu);

            }
            return true;
        }
        else
            return false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if(viewType == 0) {
            View view = layoutInflater.inflate(R.layout.item_message_sent, parent, false);
//        view.setOnClickListener(onClickListener);
            return new SentMessageHolder(view);
        }
        else if(viewType == 1){
            View view = layoutInflater.inflate(R.layout.item_message_received, parent, false);
//        view.setOnClickListener(onClickListener);
            return new ReceivedMessageHolder(view);
        }
        else {
            View view = layoutInflater.inflate(R.layout.item_message_welcome, parent, false);
//        view.setOnClickListener(onClickListener);
            return new WelcomeMessageHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && Config.welcomeMessage!=null)
            return 2; //welcomeMessage

        if(Config.welcomeMessage!=null)
            position = position - 1;

        if( mMessageList.get(position).getCustomerId() == null)
            return 1;
        else
            return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        ConversationMessage message ;

        if(Config.welcomeMessage!=null && (position == 0)){
            message = new ConversationMessage();
            message.setContent(Config.welcomeMessage);
            message.setCreatedAt("");
        }
        else if(Config.welcomeMessage != null)
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
        if(Config.welcomeMessage != null)
            return mMessageList.size() + 1;
        else
            return mMessageList.size() ;

    }
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
        }

        void bind(ConversationMessage message) {
            messageText.setText(message.getContent());
            if(Config.color!=null) {
                GradientDrawable a = (GradientDrawable) messageText.getBackground();
                a.setColor(Color.parseColor(Config.color));
//                messageText.setBackgroundColor(Color.parseColor(Config.color));
            }
            timeText.setText(Config.Message_datetime(message.getCreatedAt()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
//        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.getContent()));;
            timeText.setText(Config.Message_datetime(message.getCreatedAt()));

        }
    }
    private class WelcomeMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        WelcomeMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.getContent()));;
        }
    }
}
