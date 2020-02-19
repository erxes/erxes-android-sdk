package com.newmedia.erxeslibrary.model;

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
    public boolean isread = true;
    public List<ConversationMessage> conversationMessages = new ArrayList<>();
    public List<String> readUserIds;
    public List<User> participatedUsers;

    static public List<Conversation> convert(Response<WidgetsConversationsQuery.Data> response, Config config) {

        List<WidgetsConversationsQuery.WidgetsConversation> data = response.data().widgetsConversations();
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
            if (item.messages() != null && item.messages().size() > 0) {
                for (int i = 0; i < item.messages().size(); i++) {
                    ConversationMessage message = new ConversationMessage();
                    message.id = item.messages().get(i).fragments().messageFragment()._id();
                    message.content = item.messages().get(i).fragments().messageFragment().content();
                    message.conversationId = item.messages().get(i).fragments().messageFragment().conversationId();
                    try {
                        Date date = ErxesHelper.sdf.parse(item.messages().get(i).fragments().messageFragment().createdAt().toString());
                        message.createdAt = outputFormat.format(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        message.createdAt = item.createdAt().toString();
                    }
                    message.customerId = item.messages().get(i).fragments().messageFragment().customerId();
                    if (item.messages().get(i).fragments().messageFragment().internal() != null)
                        message.internal = item.messages().get(i).fragments().messageFragment().internal();
                    if (item.messages().get(i).fragments().messageFragment().user() != null) {
                        User user = new User();
                        user.id = item.messages().get(i).fragments().messageFragment().user()._id();
                        if (item.messages().get(i).fragments().messageFragment().user().details() != null) {
                            user.avatar = item.messages().get(i).fragments().messageFragment().user().details().avatar();
                            user.fullName = item.messages().get(i).fragments().messageFragment().user().details().fullName();
                        }
                        message.user = user;
                    }
                    if (item.messages().get(i).fragments().messageFragment().attachments() != null &&
                            item.messages().get(i).fragments().messageFragment().attachments().size() > 0) {
                        for (int j = 0; j < item.messages().get(i).fragments().messageFragment().attachments().size(); j++) {
                            FileAttachment attachment = new FileAttachment();
                            attachment.setName(item.messages().get(i).fragments().messageFragment().attachments().get(j).name());
                            attachment.setSize(item.messages().get(i).fragments().messageFragment().attachments().get(j).size());
                            attachment.setType(item.messages().get(i).fragments().messageFragment().attachments().get(j).type());
                            attachment.setUrl(item.messages().get(i).fragments().messageFragment().attachments().get(j).url());
                            message.attachments.add(attachment);
                        }
                    }

                    thisO.conversationMessages.add(message);
                }
            }
            dataConverted.add(thisO);

        }
        return dataConverted;

    }

    static public Conversation update(WidgetsInsertMessageMutation.WidgetsInsertMessage a, String content, Config config) {
        config.conversationId = a.fragments().messageFragment().conversationId();
        Conversation conversation = new Conversation();
        conversation.id = config.conversationId;
        conversation.content = content;
        conversation.status = "open";
        try {
            Date date = ErxesHelper.sdf.parse(a.fragments().messageFragment().createdAt().toString());
            conversation.date = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            conversation.date = a.fragments().messageFragment().createdAt().toString();
        }
        conversation.customerId = config.customerId;
        conversation.integrationId = config.integrationId;

        ConversationMessage message = new ConversationMessage();
        message.id = a.fragments().messageFragment()._id();
        message.content = a.fragments().messageFragment().content();
        message.conversationId = a.fragments().messageFragment().conversationId();
        try {
            Date date = ErxesHelper.sdf.parse(a.fragments().messageFragment().createdAt().toString());
            message.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            message.createdAt = a.fragments().messageFragment().createdAt().toString();
        }
        message.customerId = a.fragments().messageFragment().customerId();
        if (a.fragments().messageFragment().internal() != null)
            message.internal = a.fragments().messageFragment().internal();
        if (a.fragments().messageFragment().user() != null) {
            User user = new User();
            user.id = a.fragments().messageFragment().user()._id();
            if (a.fragments().messageFragment().user().details() != null) {
                user.avatar = a.fragments().messageFragment().user().details().avatar();
                user.fullName = a.fragments().messageFragment().user().details().fullName();
            }
            message.user = user;
        }
        if (a.fragments().messageFragment().attachments() != null &&
                a.fragments().messageFragment().attachments().size() > 0) {
            for (int j = 0; j < a.fragments().messageFragment().attachments().size(); j++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(a.fragments().messageFragment().attachments().get(j).name());
                attachment.setSize(a.fragments().messageFragment().attachments().get(j).size());
                attachment.setType(a.fragments().messageFragment().attachments().get(j).type());
                attachment.setUrl(a.fragments().messageFragment().attachments().get(j).url());
                message.attachments.add(attachment);
            }
        }
        conversation.conversationMessages.add(message);
        return conversation;
    }

}
