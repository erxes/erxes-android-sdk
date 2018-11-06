package com.newmedia.erxeslibrary.ui.conversations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.DB;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Configuration.Helper;
import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.model.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.Holder> {

    private KnowledgeBaseTopic knowledgeBaseTopic;
    private RealmList<KnowledgeBaseCategory> categories;
    private Context context;
    private Config config;
    public FaqAdapter(Context context) {
        this.context = context;
        this.config = Config.getInstance(context);
        Realm.init(context);
        Realm realm = DB.getDB();
        knowledgeBaseTopic = realm.where(KnowledgeBaseTopic.class).equalTo("_id",config.messengerdata.knowledgeBaseTopicId).findFirst();
        this.categories = knowledgeBaseTopic.categories;
        knowledgeBaseTopic.categories.addChangeListener(new RealmChangeListener<RealmList<KnowledgeBaseCategory>>() {
            @Override
            public void onChange(RealmList<KnowledgeBaseCategory> knowledgeBaseCategories) {
                FaqAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.faq_item, parent, false);
//        view.setOnClickListener(onClickListener);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
//        if(list.get(position).avatar!=null)
//            GlideApp.with(context).load(list.get(position).avatar).placeholder(R.drawable.avatar)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(holder.circleImageView);
        Log.d("xaxa",""+categories.get(position).icon);
        holder.icon.setImageResource(Helper.ICON_MAP.get(categories.get(position).icon).intValue());
        holder.title.setText(categories.get(position).title+"("+categories.get(position).numOfArticles+") ");
        holder.description.setText(categories.get(position).description);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title,description;
        View parent ;
        public Holder(View itemView) {
            super(itemView);
            parent=itemView;
            icon =  itemView.findViewById(R.id.icon);
            title =  itemView.findViewById(R.id.title);
            description =  itemView.findViewById(R.id.description);

        }
    }

}
