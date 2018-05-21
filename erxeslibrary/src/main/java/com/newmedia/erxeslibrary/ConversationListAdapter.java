package com.newmedia.erxeslibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.newmedia.erxeslibrary.Configuration.Config;
import com.newmedia.erxeslibrary.Model.Conversation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private Context context;
    private List<Conversation> conversationList;

    public void setConversationList(List<Conversation> conversationList) {
        this.conversationList = conversationList;
    }
    Realm realm =Realm.getDefaultInstance();
    public ConversationListAdapter(Context context) {
        this.context = context;

        this.conversationList =  realm.where(Conversation.class).equalTo("status","open").equalTo("customerId",Config.customerId).equalTo("integrationId",Config.integrationId).findAll();
    }
    public void update_position(String conversationId){
        for(int i = 0 ; i< conversationList.size();i++){
            if(conversationList.get(i).get_id().equalsIgnoreCase(conversationId)){
                this.notifyItemChanged(i);
                return;
            }
        }
//        int pre_size = conversationList.size();
        this.conversationList =  realm.where(Conversation.class).equalTo("status","open").equalTo("customerId",Config.customerId).equalTo("integrationId",Config.integrationId).findAll();
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
            Config.conversationId = conversationList.get((int)view.getTag()).get_id();
            context.startActivity(a);
        }
    };
    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {
//        conversationList.get(position).get
        if(conversationList.get(position).isIsread()) {

            holder.content.setText(Html.fromHtml(conversationList.get(position).getContent()));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.NORMAL);
        }
        else{
            holder.content.setText(Html.fromHtml(conversationList.get(position).getContent()));
            holder.content.setTypeface(holder.content.getTypeface(), Typeface.BOLD_ITALIC);
        }
        Long createDate = Long.valueOf(conversationList.get(position).getDate());
        holder.date.setText( Config.convert_datetime(createDate));
        holder.parent.setTag(position);
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }
}
