package com.newmedia.erxeslibrary.model;

import android.util.Log;

import com.apollographql.apollo.api.Response;

import com.erxes.io.opens.ConversationMessageInsertedSubscription;
import com.erxes.io.opens.WidgetsInsertMessageMutation;
import com.erxes.io.opens.WidgetsMessagesQuery;
import com.erxes.io.opens.type.AttachmentInput;
import com.erxes.io.saas.SaasConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.helper.ErxesHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.newmedia.erxeslibrary.helper.ErxesHelper.outputFormat;

public class ConversationMessage {
    public String id;
    public String conversationId;
    public String customerId;
    public User user;
    public String content;
    public String createdAt;
    public String contentType;
    public String vCallUrl;
    public String vCallName;
    public String vCallStatus;
    public boolean internal = false;
    public List<FileAttachment> attachments = new ArrayList<>();
    private static SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("h:mm a");


    public static List<ConversationMessage> convert(Response<WidgetsMessagesQuery.Data> response, String conversationId) {
        List<WidgetsMessagesQuery.WidgetsMessage> data = response.getData().widgetsMessages();
        List<ConversationMessage> dataConverted = new ArrayList<>();
        ConversationMessage conversationMessage;
        for (WidgetsMessagesQuery.WidgetsMessage item : data) {
            conversationMessage = new ConversationMessage();
            conversationMessage.id = item.fragments().messageFragment()._id();
            Date date = new Date();
            try {
                date = ErxesHelper.sdf.parse(item.fragments().messageFragment().createdAt().toString());
                conversationMessage.createdAt = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                date.setTime(Long.parseLong(item.fragments().messageFragment().createdAt().toString()));
                conversationMessage.createdAt = simpleDateFormat.format(date);
            }
            conversationMessage.customerId = item.fragments().messageFragment().customerId();
            conversationMessage.content = item.fragments().messageFragment().content();
            conversationMessage.internal = item.fragments().messageFragment().internal();
            if (item.fragments().messageFragment().user() != null) {
                User user = new User();
                user.setId(item.fragments().messageFragment().user()._id());
                if (item.fragments().messageFragment().user().details() != null) {
                    user.setAvatar(item.fragments().messageFragment().user().details().avatar());
                    user.setFullName(item.fragments().messageFragment().user().details().fullName());
                }
                conversationMessage.user = user;
            }
            if (item.fragments().messageFragment().attachments() != null) {
                for (int i = 0; i < item.fragments().messageFragment().attachments().size(); i++) {
                    FileAttachment attachment = new FileAttachment();
                    attachment.setName(item.fragments().messageFragment().attachments().get(i).name());
                    attachment.setSize(item.fragments().messageFragment().attachments().get(i).size());
                    attachment.setType(item.fragments().messageFragment().attachments().get(i).type());
                    attachment.setUrl(item.fragments().messageFragment().attachments().get(i).url());
                    conversationMessage.attachments.add(attachment);
                }
            }
            conversationMessage.contentType = item.fragments().messageFragment().contentType();
            if (item.fragments().messageFragment().videoCallData() != null) {
                conversationMessage.vCallName = item.fragments().messageFragment().videoCallData().name();
                conversationMessage.vCallUrl = item.fragments().messageFragment().videoCallData().url();
                conversationMessage.vCallStatus = item.fragments().messageFragment().videoCallData().status();
            }

            conversationMessage.conversationId = conversationId;
            dataConverted.add(conversationMessage);
        }
        return dataConverted;
    }

    public static ConversationMessage convert(WidgetsInsertMessageMutation.WidgetsInsertMessage insertMessage, String message, Config config) {
        ConversationMessage conversationMessage = new ConversationMessage();
        conversationMessage.conversationId = insertMessage.fragments().messageFragment().conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(insertMessage.fragments().messageFragment().createdAt().toString());
            conversationMessage.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.parseLong(insertMessage.fragments().messageFragment().createdAt().toString()));
            conversationMessage.createdAt = simpleDateFormat.format(date);
        }
        conversationMessage.id = insertMessage.fragments().messageFragment()._id();
        conversationMessage.content = message;
        if (insertMessage.fragments().messageFragment().attachments() != null) {
            for (int i = 0; i < insertMessage.fragments().messageFragment().attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(insertMessage.fragments().messageFragment().attachments().get(i).name());
                attachment.setSize(insertMessage.fragments().messageFragment().attachments().get(i).size());
                attachment.setType(insertMessage.fragments().messageFragment().attachments().get(i).type());
                attachment.setUrl(insertMessage.fragments().messageFragment().attachments().get(i).url());
                conversationMessage.attachments.add(attachment);
            }
        }
        if (insertMessage.fragments().messageFragment().internal() != null)
            conversationMessage.internal = insertMessage.fragments().messageFragment().internal();
        else conversationMessage.internal = false;
        conversationMessage.customerId = config.customerId;
        conversationMessage.contentType = insertMessage.fragments().messageFragment().contentType();
        if (insertMessage.fragments().messageFragment().videoCallData() != null) {
            conversationMessage.vCallName = insertMessage.fragments().messageFragment().videoCallData().name();
            conversationMessage.vCallUrl = insertMessage.fragments().messageFragment().videoCallData().url();
            conversationMessage.vCallStatus = insertMessage.fragments().messageFragment().videoCallData().status();
        }
        return conversationMessage;
    }

    public static ConversationMessage convert(ConversationMessageInsertedSubscription.ConversationMessageInserted messageInserted) {
        ConversationMessage conversationMessage = new ConversationMessage();
        conversationMessage.conversationId = messageInserted.fragments().messageFragment().conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(messageInserted.fragments().messageFragment().createdAt().toString());
            conversationMessage.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.parseLong(messageInserted.fragments().messageFragment().createdAt().toString()));
            conversationMessage.createdAt = simpleDateFormat.format(date);
        }
        conversationMessage.id = messageInserted.fragments().messageFragment()._id();
        conversationMessage.content = messageInserted.fragments().messageFragment().content();
        if (messageInserted.fragments().messageFragment().attachments() != null) {
            for (int i = 0; i < messageInserted.fragments().messageFragment().attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(messageInserted.fragments().messageFragment().attachments().get(i).name());
                attachment.setSize(messageInserted.fragments().messageFragment().attachments().get(i).size());
                attachment.setType(messageInserted.fragments().messageFragment().attachments().get(i).type());
                attachment.setUrl(messageInserted.fragments().messageFragment().attachments().get(i).url());
                conversationMessage.attachments.add(attachment);
            }
        }
        if (messageInserted.fragments().messageFragment().internal() != null)
            conversationMessage.internal = messageInserted.fragments().messageFragment().internal();
        else conversationMessage.internal = false;
        conversationMessage.customerId = messageInserted.fragments().messageFragment().customerId();
        if (messageInserted.fragments().messageFragment().user() != null) {
            User user = new User();
            user.setId(messageInserted.fragments().messageFragment().user()._id());
            if (messageInserted.fragments().messageFragment().user().details() != null) {
                user.setAvatar(messageInserted.fragments().messageFragment().user().details().avatar());
                user.setFullName(messageInserted.fragments().messageFragment().user().details().fullName());
            }
            conversationMessage.user = user;
        }
        conversationMessage.contentType = messageInserted.fragments().messageFragment().contentType();
        if (messageInserted.fragments().messageFragment().videoCallData() != null) {
            conversationMessage.vCallName = messageInserted.fragments().messageFragment().videoCallData().name();
            conversationMessage.vCallUrl = messageInserted.fragments().messageFragment().videoCallData().url();
            conversationMessage.vCallStatus = messageInserted.fragments().messageFragment().videoCallData().status();
        }
        return conversationMessage;
    }

    public static ConversationMessage convertSaas(SaasConversationMessageInsertedSubscription.ConversationMessageInserted messageInserted) {
        ConversationMessage conversationMessage = new ConversationMessage();
        conversationMessage.conversationId = messageInserted.conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(messageInserted.createdAt().toString());
            conversationMessage.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.parseLong(messageInserted.createdAt().toString()));
            conversationMessage.createdAt = simpleDateFormat.format(date);
        }
        conversationMessage.id = messageInserted._id();
        conversationMessage.content = messageInserted.content();
        if (messageInserted.attachments() != null) {
            for (int i = 0; i < messageInserted.attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(messageInserted.attachments().get(i).name());
                attachment.setSize(messageInserted.attachments().get(i).size());
                attachment.setType(messageInserted.attachments().get(i).type());
                attachment.setUrl(messageInserted.attachments().get(i).url());
                conversationMessage.attachments.add(attachment);
            }
        }
        if (messageInserted.internal() != null)
            conversationMessage.internal = messageInserted.internal();
        else conversationMessage.internal = false;
        conversationMessage.customerId = messageInserted.customerId();
        if (messageInserted.user() != null) {
            User user = new User();
            Map userJson = messageInserted.user().object;
            if (userJson != null) {
                if (userJson.containsKey("_id"))
                    user.setId((String) userJson.get("_id"));
                if (userJson.containsKey("details")) {
                    Map detailJson =  (Map) userJson.get("details");
                    if (detailJson != null) {
                        if (detailJson.containsKey("avatar"))
                            user.setAvatar((String) detailJson.get("avatar"));
                        if (detailJson.containsKey("fullName"))
                            user.setFullName((String) detailJson.get("fullName"));
                    }
                }
            }
            conversationMessage.user = user;
        }
        conversationMessage.contentType = messageInserted.contentType();
        if (messageInserted.videoCallData() != null) {
            conversationMessage.vCallName = messageInserted.videoCallData().name();
            conversationMessage.vCallUrl = messageInserted.videoCallData().url();
            conversationMessage.vCallStatus = messageInserted.videoCallData().status();
        }
        return conversationMessage;
    }

}
