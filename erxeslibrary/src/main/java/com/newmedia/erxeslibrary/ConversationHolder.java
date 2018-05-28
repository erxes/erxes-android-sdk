package com.newmedia.erxeslibrary;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;



public class ConversationHolder extends RecyclerView.ViewHolder {
    ImageView circleImageView;
    TextView date,content,name;
    View parent ,isonline;
    public ConversationHolder(View itemView) {
        super(itemView);
        parent=itemView;
        circleImageView =  itemView.findViewById(R.id.profile_image);
        date = itemView.findViewById(R.id.date);
        content = itemView.findViewById(R.id.content);
        name = itemView.findViewById(R.id.name);
        isonline = itemView.findViewById(R.id.isonline);
    }
}
