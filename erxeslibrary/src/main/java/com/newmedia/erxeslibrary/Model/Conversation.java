package com.newmedia.erxeslibrary.Model;



import com.apollographql.apollo.api.Response;
import com.newmedia.erxes.basic.ConversationsQuery;
import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxeslibrary.Configuration.Config;


import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Conversation extends RealmObject {
    @PrimaryKey
    public String _id;
    public String customerId;
    public String integrationId;
    public String status;
    public String content;
    public String date;
    public boolean isread = true;
    public RealmList<ConversationMessage> conversationMessages;
    public RealmList<String> readUserIds;
    public RealmList<User> participatedUsers;

    static public List<Conversation> convert(Response<ConversationsQuery.Data> response, Config config){
        List<ConversationsQuery.Conversation> data = response.data().conversations();
        List<Conversation> data_converted = new ArrayList<>();
        Conversation this_o;
        for(ConversationsQuery.Conversation item:data) {
            this_o =new Conversation();
            this_o._id = item._id();
            this_o.date = item.createdAt();
            this_o.content = item.content();
            this_o.status = item.status();
            this_o.customerId = config.customerId;
            this_o.integrationId = config.integrationId;

            data_converted.add(this_o);

        }
        return data_converted;

    }
    static public Conversation update(InsertMessageMutation.InsertMessage a, String message,Config config){
        config.conversationId = a.conversationId();
        Conversation conversation = new Conversation();
        conversation._id = config.conversationId;
        conversation.content = message;
        conversation.status = ("open");
        conversation.date = a.createdAt();
        conversation.customerId = config.customerId;
        conversation.integrationId = config.integrationId;
        return conversation;
    }

}
