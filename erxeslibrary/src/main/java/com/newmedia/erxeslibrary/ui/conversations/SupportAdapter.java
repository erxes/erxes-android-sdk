package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.Configuration.DB;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmResults;

public class SupportAdapter extends RecyclerView.Adapter<SupportAdapter.Holder> {

    private RealmResults<User> list;
    private Context context;
    public SupportAdapter(Context context) {
        this.context = context;
        Realm.init(context);
        Realm realm = DB.getDB();
        list = realm.where(User.class).findAll();
//        realm.close();
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
        if(list.get(position).avatar!=null)
            GlideApp.with(context).load(list.get(position).avatar).placeholder(R.drawable.avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.circleImageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView date,content,name;
        View parent ;//,isonline;
        public Holder(View itemView) {
            super(itemView);
            parent=itemView;
            circleImageView =  itemView.findViewById(R.id.profile_image);

        }
    }

}
