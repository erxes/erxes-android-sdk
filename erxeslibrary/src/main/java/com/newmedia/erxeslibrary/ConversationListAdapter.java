package com.newmedia.erxeslibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Configuration.GlideApp;
import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private Context context;
    public List<Conversation> conversationList;
    Realm realm;;
    Config config;
    public ConversationListAdapter(Context context) {
        this.context = context;
        Realm.init(context);
        config = Config.getInstance(context);
        realm=Realm.getDefaultInstance();
        this.conversationList =  realm.where(Conversation.class).equalTo("status","open").equalTo("customerId",config.customerId).equalTo("integrationId",config.integrationId).findAll();
    }
    public void update_position(String conversationId){
        for(int i = 0 ; i< conversationList.size();i++){
            if(conversationList.get(i)._id.equalsIgnoreCase(conversationId)){
                this.notifyItemChanged(i);
                return;
            }
        }
//        int pre_size = conversationList.size();
        this.conversationList =  realm.where(Conversation.class).equalTo("status","open").equalTo("customerId",config.customerId).equalTo("integrationId",config.integrationId).findAll();
//        this.notifyItemInserted(0);
        Log.d("ConversationListActivit","size "+conversationList.size()+" ?");


    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chatlist_item, parent, false);
        view.setOnClickListener(onClickListener);
        return new ConversationHolder(view);
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent a = new Intent(context,MessageActivity.class);
            config.conversationId = conversationList.get((int)view.getTag())._id;
            context.startActivity(a);
        }
    };
    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {
//        conversationList.get(position).get
        if(conversationList.get(position).isread) {
            if(conversationList.get(position).content!=null)
            holder.content.setText(Html.fromHtml(conversationList.get(position).content));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
            holder.name.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
            holder.content.setTextColor(Color.parseColor("#808080"));
        }
        else{
            holder.content.setText(Html.fromHtml(conversationList.get(position).content));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.BOLD);
            holder.name.setTypeface(holder.content.getTypeface(), Typeface.BOLD);
            holder.content.setTextColor(Color.BLACK);
        }
//        if(!Config.IsMessengerOnline)
//            holder.isonline.setVisibility(View.GONE);
//        else
//            holder.isonline.setVisibility(View.VISIBLE);
        ConversationMessage message = realm.where(ConversationMessage.class).equalTo("conversationId",conversationList.get(position)._id).isNotNull("user").sort("createdAt", Sort.DESCENDING).findFirst();
        holder.circleImageView.setImageResource(R.drawable.avatar);
        holder.name.setText("");
        if(message!=null&&message.user !=null){
            String myString = message.user.fullName;
            String upperString = myString.substring(0,1).toUpperCase() + myString.substring(1);
            holder.name.setText(upperString);
            if(message.user.avatar!=null)
                GlideApp.with(context).load(message.user.avatar).placeholder(R.drawable.avatar)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.circleImageView);

            Long createDate = Long.valueOf(message.createdAt);
            holder.date.setText(config.convert_datetime(createDate));
            holder.parent.setTag(position);
        }else {
            Long createDate = Long.valueOf(conversationList.get(position).date);
            holder.date.setText(config.convert_datetime(createDate));
            holder.parent.setTag(position);
        }

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }
}
