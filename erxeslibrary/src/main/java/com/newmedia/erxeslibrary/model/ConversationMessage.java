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
    public boolean internal = false;
    public List<FileAttachment> attachments = new ArrayList<>();
    private static SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat("h:mm a");


    public static List<ConversationMessage> convert(Response<WidgetsMessagesQuery.Data> response, String conversationId) {
        List<WidgetsMessagesQuery.WidgetsMessage> data = response.data().widgetsMessages();
        List<ConversationMessage> dataConverted = new ArrayList<>();
        ConversationMessage thisO;
        for (WidgetsMessagesQuery.WidgetsMessage item : data) {
            thisO = new ConversationMessage();
            thisO.id = item.fragments().messageFragment()._id();
            Date date = new Date();
            try {
                date = ErxesHelper.sdf.parse(item.fragments().messageFragment().createdAt().toString());
                thisO.createdAt = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                date.setTime(Long.valueOf(Objects.requireNonNull(item.fragments().messageFragment().createdAt()).toString()));
                thisO.createdAt = simpleDateFormat.format(date);
            }
            thisO.customerId = item.fragments().messageFragment().customerId();
            thisO.content = item.fragments().messageFragment().content();
            thisO.internal = item.fragments().messageFragment().internal();
            if (item.fragments().messageFragment().user() != null) {
                User user = new User();
                user.id = item.fragments().messageFragment().user()._id();
                user.avatar = item.fragments().messageFragment().user().details().avatar();
                user.fullName = item.fragments().messageFragment().user().details().fullName();
                thisO.user = user;
            }
            if (item.fragments().messageFragment().attachments() != null) {
                for (int i = 0; i < item.fragments().messageFragment().attachments().size(); i++) {
                    FileAttachment attachment = new FileAttachment();
                    attachment.setName(item.fragments().messageFragment().attachments().get(i).name());
                    attachment.setSize(item.fragments().messageFragment().attachments().get(i).size());
                    attachment.setType(item.fragments().messageFragment().attachments().get(i).type());
                    attachment.setUrl(item.fragments().messageFragment().attachments().get(i).url());
                    thisO.attachments.add(attachment);
                }
            }
            thisO.conversationId = conversationId;
            dataConverted.add(thisO);
        }
        return dataConverted;
    }

    public static ConversationMessage convert(WidgetsInsertMessageMutation.WidgetsInsertMessage a, String message, Config config) {
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.fragments().messageFragment().conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(a.fragments().messageFragment().createdAt().toString());
            b.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.valueOf(Objects.requireNonNull(a.fragments().messageFragment().createdAt()).toString()));
            b.createdAt = simpleDateFormat.format(date);
        }
        b.id = a.fragments().messageFragment()._id();
        b.content = message;
        if (a.fragments().messageFragment().attachments() != null) {
            for (int i = 0; i < a.fragments().messageFragment().attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(a.fragments().messageFragment().attachments().get(i).name());
                attachment.setSize(a.fragments().messageFragment().attachments().get(i).size());
                attachment.setType(a.fragments().messageFragment().attachments().get(i).type());
                attachment.setUrl(a.fragments().messageFragment().attachments().get(i).url());
                b.attachments.add(attachment);
            }
        }
        if (a.fragments().messageFragment().internal() != null)
            b.internal = a.fragments().messageFragment().internal();
        else b.internal = false;
        b.customerId = config.customerId;
        return b;
    }

    public static ConversationMessage convert(ConversationMessageInsertedSubscription.ConversationMessageInserted a) {
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(a.createdAt().toString());
            b.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.valueOf(Objects.requireNonNull(a.createdAt()).toString()));
            b.createdAt = simpleDateFormat.format(date);
        }
        b.id = a._id();
        b.content = a.content();
        if (a.attachments() != null) {
            for (int i = 0; i < a.attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(a.attachments().get(i).name());
                attachment.setSize(a.attachments().get(i).size());
                attachment.setType(a.attachments().get(i).type());
                attachment.setUrl(a.attachments().get(i).url());
                b.attachments.add(attachment);
            }
        }
        if (a.internal() != null)
            b.internal = a.internal();
        else b.internal = false;
        b.customerId = a.customerId();
        if (a.user() != null) {
            User user = new User();
            user.id = a.user()._id();
            user.avatar = a.user().details().avatar();
            user.fullName = a.user().details().fullName();
            b.user = user;
        }
        return b;
    }

    public static ConversationMessage convertSaas(SaasConversationMessageInsertedSubscription.ConversationMessageInserted a) {
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        Date date = new Date();
        try {
            date = ErxesHelper.sdf.parse(a.createdAt().toString());
            b.createdAt = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            date.setTime(Long.valueOf(Objects.requireNonNull(a.createdAt()).toString()));
            b.createdAt = simpleDateFormat.format(date);
        }
        b.id = a._id();
        b.content = a.content();
        if (a.attachments() != null) {
            for (int i = 0; i < a.attachments().size(); i++) {
                FileAttachment attachment = new FileAttachment();
                attachment.setName(a.attachments().get(i).name());
                attachment.setSize(a.attachments().get(i).size());
                attachment.setType(a.attachments().get(i).type());
                attachment.setUrl(a.attachments().get(i).url());
                b.attachments.add(attachment);
            }
        }
        if (a.internal() != null)
            b.internal = a.internal();
        else b.internal = false;
        b.customerId = a.customerId();
        if (a.user() != null) {
            User user = new User();
            Map userJson = a.user().object;
            if (userJson != null) {
                if (userJson.containsKey("_id"))
                    user.id = userJson.get("_id").toString();
                if (userJson.containsKey("details")) {
                    Map detailJson =  (Map) userJson.get("details");
                    if (detailJson != null) {
                        if (detailJson.containsKey("avatar"))
                            user.avatar = detailJson.get("avatar").toString();
                        if (detailJson.containsKey("fullName"))
                            user.fullName = detailJson.get("fullName").toString();
                    }
                }
            }
            b.user = user;
        }
        return b;
    }

}
