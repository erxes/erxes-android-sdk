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
    public String contentType;
    public String vCallUrl;
    public String vCallName;
    public String vCallStatus;
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
                        user.setId(item.messages().get(i).fragments().messageFragment().user()._id());
                        if (item.messages().get(i).fragments().messageFragment().user().details() != null) {
                            user.setAvatar(item.messages().get(i).fragments().messageFragment().user().details().avatar());
                            user.setFullName(item.messages().get(i).fragments().messageFragment().user().details().fullName());
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

                    message.contentType = item.messages().get(i).fragments().messageFragment().contentType();
                    if (item.messages().get(i).fragments().messageFragment().videoCallData() != null) {
                        message.vCallUrl = item.messages().get(i).fragments().messageFragment().videoCallData().url();
                        message.vCallStatus = item.messages().get(i).fragments().messageFragment().videoCallData().status();
                        message.vCallName = item.messages().get(i).fragments().messageFragment().videoCallData().name();
                    }

                    thisO.conversationMessages.add(message);
                }
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

        ConversationMessage message = new ConversationMessage();
        message.id = insertMessage.fragments().messageFragment()._id();
        message.content = insertMessage.fragments().messageFragment().content();
        message.conversationId = insertMessage.fragments().messageFragment().conversationId();
        try {
            Date date = ErxesHelper.sdf.parse(insertMessage.fragments().messageFragment().createdAt().toString());
            message.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            message.createdAt = insertMessage.fragments().messageFragment().createdAt().toString();
        }
        message.customerId = insertMessage.fragments().messageFragment().customerId();
        if (insertMessage.fragments().messageFragment().internal() != null)
            message.internal = insertMessage.fragments().messageFragment().internal();
        if (insertMessage.fragments().messageFragment().user() != null) {
            User user = new User();
            user.setId(insertMessage.fragments().messageFragment().user()._id());
            if (insertMessage.fragments().messageFragment().user().details() != null) {
                user.setAvatar(insertMessage.fragments().messageFragment().user().details().avatar());
                user.setFullName(insertMessage.fragments().messageFragment().user().details().fullName());
            }
            message.user = user;
        }
        if (insertMessage.fragments().messageFragment().attachments() != null &&
                insertMessage.fragments().messageFragment().attachments().size() > 0) {
            for (int j = 0; j < insertMessage.fragments().messageFragment().attachments().size(); j++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(insertMessage.fragments().messageFragment().attachments().get(j).name());
                attachment.setSize(insertMessage.fragments().messageFragment().attachments().get(j).size());
                attachment.setType(insertMessage.fragments().messageFragment().attachments().get(j).type());
                attachment.setUrl(insertMessage.fragments().messageFragment().attachments().get(j).url());
                message.attachments.add(attachment);
            }
        }
        message.contentType = insertMessage.fragments().messageFragment().contentType();
        if (insertMessage.fragments().messageFragment().videoCallData() != null) {
            message.vCallUrl = insertMessage.fragments().messageFragment().videoCallData().url();
            message.vCallName = insertMessage.fragments().messageFragment().videoCallData().name();
            message.vCallStatus = insertMessage.fragments().messageFragment().videoCallData().status();
        }

        conversation.conversationMessages.add(message);
        return conversation;
    }

}
