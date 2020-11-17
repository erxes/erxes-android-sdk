package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.model.User;

import java.util.List;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.Holder> {

    private final Config config;
    private final List<User> list;
    private final Context context;

    public SupportAdapter(Activity context, Config config) {
        this.context = context;
        this.list = config.supporters;
        this.config = config;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.profile_image_online, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (config.isOnline) {
            holder.activeView.setBackground(context.getDrawable(R.drawable.circle_active));
        } else {
            holder.activeView.setBackground(context.getDrawable(R.drawable.circle_inactive));
        }
        if (list.get(position).getAvatar() != null)
            Glide.with(context)
                    .load(list.get(position).getAvatar())
                    .placeholder(R.drawable.avatar)
                    .optionalCircleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);
        else {
            Glide.with(context)
                    .load(R.drawable.avatar)
                    .optionalCircleCrop()
                    .into(holder.circleImageView);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView circleImageView;
        TextView date, content, name;
        View parent,activeView;

        public Holder(View itemView) {
            super(itemView);
            parent = itemView;
            circleImageView = itemView.findViewById(R.id.profile_image);
            activeView = itemView.findViewById(R.id.activeView);
        }
    }

}
