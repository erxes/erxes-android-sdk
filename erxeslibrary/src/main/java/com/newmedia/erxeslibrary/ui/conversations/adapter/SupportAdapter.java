package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.User;

import java.util.ArrayList;
import java.util.List;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.Holder> {

    private List<User> list = new ArrayList<>();
    private Context context;

    public SupportAdapter(Activity context,List<User> users) {
        this.context = context;
        list = users;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.profile_image_online, parent, false);
//        view.setOnClickListener(onClickListener);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (list.get(position).avatar != null)
            Glide.with(context).load(list.get(position).avatar)
                    .placeholder(R.drawable.avatar)
                    .optionalCircleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView circleImageView;
        TextView date, content, name;
        View parent;//,isonline;

        public Holder(View itemView) {
            super(itemView);
            parent = itemView;
            circleImageView = itemView.findViewById(R.id.profile_image);

        }
    }

}
