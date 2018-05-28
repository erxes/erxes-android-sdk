package com.newmedia.erxeslibrary.Model;



import com.apollographql.apollo.api.Response;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxeslibrary.Configuration.Config;


import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Conversation extends RealmObject {
    @PrimaryKey
    private String _id;
    private String customerId;
    private String integrationId;
    private String status;
    private String content;
    private String date;
    private boolean isread = true;
    private RealmList<ConversationMessage> conversationMessages;
    private RealmList<String> readUserIds;
    private RealmList<User> participatedUsers;
    public boolean isIsread() {
        return isread;
    }

    public void setIsread(boolean isread) {
        this.isread = isread;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public void setIntegrationId(String integrationId) {
        this.integrationId = integrationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public RealmList<ConversationMessage> getConversationMessages() {
        return conversationMessages;
    }

    public void setConversationMessages(RealmList<ConversationMessage> conversationMessages) {
        this.conversationMessages = conversationMessages;
    }

    public RealmList<String> getReadUserIds() {
        return readUserIds;
    }

    public void setReadUserIds(RealmList<String> readUserIds) {
        this.readUserIds = readUserIds;
    }

    public RealmList<User> getParticipatedUsers() {
        return participatedUsers;
    }

    public void setParticipatedUsers(RealmList<User> participatedUsers) {
        this.participatedUsers = participatedUsers;
    }

    static public List<Conversation> convert(Response<ConversationsQuery.Data> response){
        List<ConversationsQuery.Conversation> data = response.data().conversations();
        List<Conversation> data_converted = new ArrayList<>();
        Conversation this_o;
        for(ConversationsQuery.Conversation item:data) {
            this_o =new Conversation();
            this_o.set_id(item._id());
            this_o.setDate(item.createdAt());
            this_o.setContent(item.content());
            this_o.setStatus(item.status());
            this_o.setCustomerId(Config.customerId);
            this_o.setIntegrationId(Config.integrationId);

            data_converted.add(this_o);

        }
        return data_converted;

    }

}
