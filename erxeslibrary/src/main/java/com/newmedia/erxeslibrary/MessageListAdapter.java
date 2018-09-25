package com.newmedia.erxeslibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
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
    private Config config;
    public MessageListAdapter( Context context,List<ConversationMessage> mMessageList) {
        this.context = context;
        this.config = Config.getInstance(context);
        this.mMessageList =  mMessageList;
        this.previous_size = this.mMessageList.size();
    }

    public void setmMessageList(List<ConversationMessage> mMessageList) {
        this.mMessageList = mMessageList;
    }
    public boolean IsBeginningChat(){
        if(mMessageList.size() == 0)
            return true;
        else
            return false;
    }

    public boolean refresh_data(){

        if(mMessageList.size() > previous_size) {
            int counter_before = mMessageList.size();
            int zoruu = mMessageList.size() - previous_size;

            previous_size = mMessageList.size();
            if(config.welcomeMessage!=null) {
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
        if(position == 0 && config.welcomeMessage!=null)
            return 2; //welcomeMessage

        if(config.welcomeMessage!=null)
            position = position - 1;

        if( config.customerId.equalsIgnoreCase(mMessageList.get(position).customerId ))
            return 0;
        else
            return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        ConversationMessage message ;

        if(config.welcomeMessage!=null && (position == 0)){
            message = new ConversationMessage();
            message.content = (config.welcomeMessage);
            message.createdAt = ("");
        }
        else if(config.welcomeMessage != null)
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
        if(config.welcomeMessage != null)
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
        TextView messageText, timeText;
        ViewGroup filelist;
        SentMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            filelist = itemView.findViewById(R.id.filelist);
        }

        void bind(ConversationMessage message) {
            messageText.setText(Html.fromHtml(message.content));;
            timeText.setText(config.Message_datetime(message.createdAt));
            GradientDrawable a1 = (GradientDrawable) messageText.getBackground();
            a1.setColor(config.colorCode);
//                messageText.setBackgroundColor(Color.parseColor(Config.color));
            filelist.removeAllViews();
            timeText.setText(config.Message_datetime(message.createdAt));
            if(message.attachments !=null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);

                try {

                    JSONArray a = new JSONArray(message.attachments);
                    for (int i = 0; i < a.length(); i++) {
                        View view = layoutInflater.inflate(R.layout.file_item, filelist, false);
                        draw_file(a.getJSONObject(i),
                                (ImageView) view.findViewById(R.id.image_input),
                                view,
                                (TextView) view.findViewById(R.id.filename));
                        filelist.addView(view);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView  timeText;
        TextView messageText;
        ImageView profileImage;
        ViewGroup filelist;


        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText =  itemView.findViewById(R.id.text_message_body);
            timeText =  itemView.findViewById(R.id.text_message_time);
            profileImage = itemView.findViewById(R.id.image_message_profile);
            filelist = itemView.findViewById(R.id.filelist);

        }

        void bind(ConversationMessage message) {
//            messageText.loadData(message.content,"text/html","utf-8");
            messageText.setText(Html.fromHtml(message.content.replace("\n","")));;
//            messageText.setText(message.content);;
            timeText.setText(config.Message_datetime(message.createdAt));

/**/
            if(message.user!=null){

                GlideApp.with(context).load(message.user.avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImage);
            }
            else
                profileImage.setImageResource(R.drawable.avatar);

            filelist.removeAllViews();
            timeText.setText(config.Message_datetime(message.createdAt));
            if(message.attachments !=null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);

                try {

                    JSONArray a = new JSONArray(message.attachments);
                    for (int i = 0; i < a.length(); i++) {
                        View view = layoutInflater.inflate(R.layout.file_item, filelist, false);
                        draw_file(a.getJSONObject(i),
                                (ImageView) view.findViewById(R.id.image_input),
                                view,
                                (TextView) view.findViewById(R.id.filename));
                        filelist.addView(view);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

            float scale = context.getResources().getDisplayMetrics().density;
            int pixels = (int) (20 * scale + 0.5f);
            inputImage.getLayoutParams().width = pixels;
            inputImage.requestLayout();

            inputImage.setImageDrawable(circularProgressDrawable);

            inputImage.getDrawable().setColorFilter(config.colorCode, PorterDuff.Mode.SRC_ATOP);

            fileview.setTag(url);
            fileview.setOnClickListener(fileDownload);

            filename.setText(name);
            filename.setVisibility(View.VISIBLE);



            if(type.contains("image")) {
                pixels = (int) (200 * scale + 0.5f);
                inputImage.getLayoutParams().width = pixels;
//                inputImage.getLayoutParams().height = pixels;
                inputImage.requestLayout();

                GlideApp.with(context).load(url).placeholder(circularProgressDrawable)
                        .diskCacheStrategy(DiskCacheStrategy.ALL).override(pixels,Target.SIZE_ORIGINAL)
                        .into(inputImage);
                fileview.setOnClickListener(null);
                filename.setVisibility(View.GONE);
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
//    private SimpleTarget target = new SimpleTarget<Bitmap>() {
//        @Override
//        public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
//            // do something with the bitmap
//            // set it to an ImageView
//            inputImage.setImageBitmap(bitmap);
//        }
//    };
}
