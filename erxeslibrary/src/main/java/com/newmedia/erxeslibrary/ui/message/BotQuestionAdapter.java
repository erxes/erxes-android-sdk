package com.newmedia.erxeslibrary.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.DraweeView;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.connection.WidgetBotRequest;
import com.newmedia.erxeslibrary.model.FileAttachment;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BotQuestionAdapter extends RecyclerView.Adapter {

    private final int TYPE1 = 0;
    private final GenericDraweeHierarchyBuilder builder;
    private final Activity activity;
    private List<ElementItem> maps = new ArrayList<>();
    private final Config config;
    private String title;
    private WidgetBotRequest widgetBotRequest;
    private ErxesRequest erxesRequest;
    private String BOT_TYPE = "";
    BotQuestionAdapter(Activity activity,String title, List<Map> items) {
        this.activity = activity;
        this.title = title;
        for(int i = 0 ; i < items.size(); i++){
            String title1 = (String)items.get(i).get("title");
            String payload = (String)items.get(i).get("payload");
            String type = items.get(i).containsKey("type") ? (String) items.get(i).get("type") : "custom";
            BOT_TYPE = type;
            ElementItem item = new ElementItem(type,title1,payload);
            maps.add(item);
        }
        this.config = Config.getInstance(activity);
        erxesRequest = ErxesRequest.getInstance(config);

        Drawable failureDrawable = activity.getResources().getDrawable(R.drawable.ic_error_black_96dp);
        DrawableCompat.setTint(failureDrawable, Color.RED);

        ProgressBarDrawable progressBarDrawable = new ProgressBarDrawable();
        progressBarDrawable.setColor(config.colorCode);
        progressBarDrawable.setBackgroundColor(config.textColorCode);

        progressBarDrawable
                .setRadius(activity.getResources().getDimensionPixelSize(R.dimen.drawee_hierarchy_progress_radius));

        builder = new GenericDraweeHierarchyBuilder(activity.getResources());
        builder.setPlaceholderImage(config.getImageVIcon(activity,config.colorCode));
        builder.setProgressBarImage(progressBarDrawable);
        builder.setFailureImage(failureDrawable, ScalingUtils.ScaleType.CENTER_INSIDE);

        int color = activity.getResources().getColor(R.color.black_10);
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(16);
        roundingParams.setBorder(color,1);
        builder.setRoundingParams(roundingParams);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bot_press_item, viewGroup, false);
        vh = new BotQuestionAdapter.OtherViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(BOT_TYPE.contentEquals("custom")){
            ((BotQuestionAdapter.OtherViewHolder) viewHolder).bind(maps.get(i), i);
            return;
        }
        if(i == 0 ){
            ElementItem item =  new ElementItem(null,title,null);
            ((BotQuestionAdapter.OtherViewHolder) viewHolder).bind(item, i);
        } else {
            ((BotQuestionAdapter.OtherViewHolder) viewHolder).bind(maps.get(i-1), i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE1;
    }

    @Override
    public int getItemCount() {
        if(BOT_TYPE.contentEquals("custom")){
            return maps != null ? maps.size() : 0;
        }
        int x = maps != null ? maps.size()+1 : 0;
        return x;
    }

    class OtherViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        OtherViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ElementItem item = (ElementItem) view.getTag();
                erxesRequest.sendWidgetBotRequest(item.title,item.type,item.payload);
            }
        };
        void bind(final ElementItem element, int position) {
            if(position==0 && !BOT_TYPE.contentEquals("custom")){
                textView.setTypeface(null, Typeface.BOLD);
                textView.setOnClickListener(null);
            }else{
                textView.setTag(element);
                textView.setTypeface(null, Typeface.NORMAL);
                textView.setOnClickListener(onClickListener);
            }
            textView.setText(element.title);
        }
    }
    class ElementItem {
        String type = "";
        String title = "";
        String payload = "";
        public ElementItem(String type, String title, String payload) {
            this.type = type;
            this.title = title;
            this.payload = payload;
        }
    }
}
