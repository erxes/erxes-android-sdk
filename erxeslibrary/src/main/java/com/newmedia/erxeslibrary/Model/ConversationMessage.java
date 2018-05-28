package com.newmedia.erxeslibrary.Model;

import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.MessagesQuery;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ConversationMessage extends RealmObject {
    @PrimaryKey
    private String _id;
    private String conversationId;
    private String customerId;
    private User user;
    private String content;
    private String createdAt;
    private boolean internal;
    private String attachments;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    static public List<ConversationMessage> convert(Response<MessagesQuery.Data> response,String ConversationId){
        List<MessagesQuery.Message> data = response.data().messages();
        List<ConversationMessage> data_converted = new ArrayList<>();
        ConversationMessage this_o;
        for(MessagesQuery.Message item:data) {
            this_o = new ConversationMessage();
            this_o.set_id(item._id());
            this_o.setCreatedAt(item.createdAt());
            this_o.setCustomerId(item.customerId());
            this_o.setContent(item.content());
            this_o.setInternal(item.internal());
            if(item.user()!=null){
                Log.d("message","user"+item.user().details().avatar());
                User user = new User();
                user.convert(item.user());
                this_o.setUser(user);
            }
            if(item.attachments()!=null) {
                this_o.setAttachments(item.attachments().toString());
            }
            this_o.setConversationId(ConversationId);


            data_converted.add(this_o);
        }
        return data_converted;

    }
}
