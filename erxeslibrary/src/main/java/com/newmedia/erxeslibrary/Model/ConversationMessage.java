package com.newmedia.erxeslibrary.Model;

import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.Configuration.Config;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ConversationMessage extends RealmObject {
    @PrimaryKey
    public String _id;
    public String conversationId;
    public String customerId;
    public User user;
    public String content;
    public String createdAt;
    public boolean internal;
    public String attachments;

    
    static public List<ConversationMessage> convert(Response<MessagesQuery.Data> response,String ConversationId){
        List<MessagesQuery.Message> data = response.data().messages();
        List<ConversationMessage> data_converted = new ArrayList<>();
        ConversationMessage this_o;
        for(MessagesQuery.Message item:data) {
            this_o = new ConversationMessage();
            this_o._id = item._id();
            this_o.createdAt = item.createdAt();
            this_o.customerId  = item.customerId();
            this_o.content = item.content();
            this_o.internal = item.internal();
            if(item.user()!=null){
//                Log.d("message","user"+item.user().details().avatar());
                User user = new User();
                user.convert(item.user());
                this_o.user = user;
            }
            if(item.attachments()!=null) {
                this_o.attachments = item.attachments().toString();
            }
            this_o.conversationId = ConversationId;


            data_converted.add(this_o);
        }
        return data_converted;

    }
    static public ConversationMessage convert(InsertMessageMutation.InsertMessage a,String message){
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b._id = a._id();
        b.content = message;
        if(a.attachments()!=null)
            b.attachments = a.attachments().toString();
        b.internal = false;
        b.customerId = Config.customerId;//Config.customerId;
        return b;
    }
    static public ConversationMessage convert(ConversationMessageInsertedSubscription.ConversationMessageInserted a){
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b._id = a._id();
        b.content = a.content();
        if(a.attachments()!=null)
            b.attachments = a.attachments().toString();
        b.internal = false;
        b.customerId = a.customerId();
        if(a.user() != null) {
            User user = new User();
            user.convert(a.user());
            b.user = user;
        }
        return b;
    }

}
