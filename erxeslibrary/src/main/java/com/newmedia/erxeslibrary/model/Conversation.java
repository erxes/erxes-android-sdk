package com.newmedia.erxeslibrary.model;

import android.util.Log;

import com.apollographql.apollo.api.Response;
import com.erxes.io.opens.WidgetsConversationsQuery;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.newmedia.erxeslibrary.helper.ErxesHelper.outputFormat;

public class Conversation {
    public String id;
    public String customerId;
    public String integrationId;
    public String status;
    public String content;
    public String date;
    public String contentType;
    public boolean isread = true;
    public List<String> readUserIds;
    public List<User> participatedUsers = new ArrayList<>();

    static public List<Conversation> convert(Response<WidgetsConversationsQuery.Data> response, Config config) {

        List<WidgetsConversationsQuery.WidgetsConversation> data = response.getData().widgetsConversations();
        List<Conversation> dataConverted = new ArrayList<>();
        Conversation thisO;
        ErxesHelper.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        for (WidgetsConversationsQuery.WidgetsConversation item : data) {
            thisO = new Conversation();
            thisO.id = item._id();
            try {
                Date date = ErxesHelper.sdf.parse(item.createdAt().toString());
                thisO.date = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                thisO.date = item.createdAt().toString();
            }

            thisO.content = item.content();
            thisO.status = item.status();
            thisO.customerId = item.customerId();
            thisO.integrationId = item.integrationId();
            if (item.participatedUsers() != null && item.participatedUsers().size() > 0) {
                User user = new User();
                user.setAvatar(item.participatedUsers().get(0).details().avatar());
                user.setFullName(item.participatedUsers().get(0).details().fullName());
                thisO.participatedUsers.add(user);
            }

            dataConverted.add(thisO);

        }
        return dataConverted;

    }

    static public Conversation update(WidgetsInsertMessageMutation.WidgetsInsertMessage insertMessage, String content, Config config) {
        insertMessage.fragments().messageFragment().contentType();
        config.conversationId = insertMessage.fragments().messageFragment().conversationId();
        Conversation conversation = new Conversation();
        conversation.id = config.conversationId;
        conversation.content = content;
        conversation.status = "open";
        try {
            Date date = ErxesHelper.sdf.parse(insertMessage.fragments().messageFragment().createdAt().toString());
            conversation.date = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            conversation.date = insertMessage.fragments().messageFragment().createdAt().toString();
        }
        conversation.customerId = config.customerId;
        conversation.integrationId = config.integrationId;

        return conversation;
    }

}
