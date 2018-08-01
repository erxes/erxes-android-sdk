package com.newmedia.erxeslibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {


    private List<ConversationMessage> mMessageList;
    private Context context;
    private int previous_size = 0;
    public MessageListAdapter( Context context,List<ConversationMessage> mMessageList) {
        this.context = context;
        this.mMessageList =  mMessageList;
        this.previous_size = this.mMessageList.size();
    }

    public void setmMessageList(List<ConversationMessage> mMessageList) {
        this.mMessageList = mMessageList;
    }

    public boolean refresh_data(){

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

        if( mMessageList.get(position).customerId == null)
            return 1;
        else
            return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        ConversationMessage message ;

        if(Config.welcomeMessage!=null && (position == 0)){
            message = new ConversationMessage();
            message.content = (Config.welcomeMessage);
            message.createdAt = ("");
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
    private View.OnClickListener fileDownload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String url = (String)view.getTag();
            if(url.startsWith("http")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) view.getTag()));
                context.startActivity(browserIntent);
            }
        }
    };
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText,filename;
        ImageView inputImage;
        View fileview;
        SentMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            inputImage = itemView.findViewById(R.id.image_input);
            fileview = itemView.findViewById(R.id.fileview);
            filename = itemView.findViewById(R.id.filename);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.content));;
            timeText.setText(Config.Message_datetime(message.createdAt));
            inputImage.setImageResource(0);
            fileview.setVisibility(View.GONE);
            filename.setText("");

            if(Config.color!=null) {
                GradientDrawable a = (GradientDrawable) messageText.getBackground();
                a.setColor(Color.parseColor(Config.color));
//                messageText.setBackgroundColor(Color.parseColor(Config.color));
            }
            timeText.setText(Config.Message_datetime(message.createdAt));
            if(message.attachments !=null) {
                try {

                    JSONArray a = new JSONArray(message.attachments);
                    for (int i = 0; i < a.length(); i++) {
                        fileview.setVisibility(View.VISIBLE);
                        draw_file(a.getJSONObject(i), inputImage, fileview, filename);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText,filename;
        ImageView profileImage,inputImage;
        View fileview;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            inputImage = itemView.findViewById(R.id.image_input);
            profileImage = itemView.findViewById(R.id.image_message_profile);
            filename = itemView.findViewById(R.id.filename);
            fileview = itemView.findViewById(R.id.fileview);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.content));;
            timeText.setText(Config.Message_datetime(message.createdAt));
            inputImage.setImageResource(0);
            fileview.setVisibility(View.GONE);
            filename.setText("");

            if(message.user!=null){

                GlideApp.with(context).load(message.user.avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }
            else
                profileImage.setImageResource(R.drawable.avatar);

            if(message.attachments !=null)
                try {
                    JSONArray a = new JSONArray(message.attachments);
                    for(int i=0;i< a.length();i++) {
                        fileview.setVisibility(View.VISIBLE);
                        draw_file(a.getJSONObject(i), inputImage, fileview, filename);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }
    }
    private class WelcomeMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        WelcomeMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.content));;
        }
    }
    private void draw_file(JSONObject o,ImageView inputImage,View fileview,TextView filename){


        try{
            String type = o.getString("type");
            String size = o.getString("size");
            String name = o.getString("name");
            String url = o.getString("url");
            CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
            circularProgressDrawable.setStrokeWidth(  5f);
            circularProgressDrawable.setCenterRadius(  30f);
            circularProgressDrawable.start();
            final float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (200 * scale + 0.5f);
            inputImage.getLayoutParams().width = pixels;
            inputImage.setImageDrawable(circularProgressDrawable);
            GlideApp.with(context).load(url).placeholder(circularProgressDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(inputImage);
            fileview.setOnClickListener(null);
            filename.setVisibility(View.GONE);
            if(type.contains("image")){

            }
            else if(type.contains("application/pdf")){
                inputImage.setImageResource(R.drawable.filepdf);
            }
            else if(type.contains("application")&&type.contains("word")){
                inputImage.setImageResource(R.drawable.fileword);
            }
            else{
                inputImage.setImageResource(R.drawable.file);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
