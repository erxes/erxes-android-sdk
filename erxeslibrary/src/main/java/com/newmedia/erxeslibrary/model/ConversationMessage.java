package com.newmedia.erxeslibrary.model;

import com.apollographql.apollo.api.Response;

import com.newmedia.erxes.basic.InsertMessageMutation;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;
import com.newmedia.erxeslibrary.configuration.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationMessage {
    public String id;
    public String conversationId;
    public String customerId;
    public User user;
    public String content;
    public String createdAt;
    public boolean internal = false;
    public String attachments;


    public static List<ConversationMessage> convert(Response<MessagesQuery.Data> response, String ConversationId) {
        List<MessagesQuery.Message> data = response.data().messages();
        List<ConversationMessage> dataConverted = new ArrayList<>();
        ConversationMessage thisO;
        for (MessagesQuery.Message item : data) {
            thisO = new ConversationMessage();
            thisO.id = item._id();
            thisO.createdAt = item.createdAt();
            thisO.customerId = item.customerId();
            thisO.content = item.content();
            thisO.internal = item.internal();
            if (item.user() != null) {
                User user = new User();
                user.convert(item.user());
                thisO.user = user;
            }
            if (item.attachments() != null) {
                JSONArray array = new JSONArray();
                for (int i = 0; i < item.attachments().size(); i++) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("name", item.attachments().get(i).name());
                        json.put("size", item.attachments().get(i).size());
                        json.put("url", item.attachments().get(i).url());
                        json.put("type", item.attachments().get(i).type());
                        array.put(i, json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                thisO.attachments = array.toString();
            }
            thisO.conversationId = ConversationId;
            dataConverted.add(thisO);
        }
        return dataConverted;
    }

    public static ConversationMessage convert(InsertMessageMutation.InsertMessage a, String message, Config config) {
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b.id = a._id();
        b.content = message;
        if (a.attachments() != null) {
            JSONArray array = new JSONArray();
            for (int i = 0; i < a.attachments().size(); i++) {
                JSONObject json = new JSONObject();
                try {
                    json.put("name", a.attachments().get(i).name());
                    json.put("size", a.attachments().get(i).size());
                    json.put("url", a.attachments().get(i).url());
                    json.put("type", a.attachments().get(i).type());
                    array.put(i, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            b.attachments = array.toString();
        }
        if (a.internal() != null)
            b.internal = a.internal();
        else b.internal = false;
        b.customerId = config.customerId;
        return b;
    }

    public static ConversationMessage convert(ConversationMessageInsertedSubscription.ConversationMessageInserted a) {
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b.id = a._id();
        b.content = a.content();
        if (a.attachments() != null) {
            JSONArray array = new JSONArray();
            for (int i = 0; i < a.attachments().size(); i++) {
                JSONObject json = new JSONObject();
                try {
                    json.put("name", a.attachments().get(i).name());
                    json.put("size", a.attachments().get(i).size());
                    json.put("url", a.attachments().get(i).url());
                    json.put("type", a.attachments().get(i).type());
                    array.put(i, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            b.attachments = array.toString();
        }
        if (a.internal() != null)
            b.internal = a.internal();
        else b.internal = false;
        b.customerId = a.customerId();
        if (a.user() != null) {
            User user = new User();
            user.convert(a.user());
            b.user = user;
        }
        return b;
    }

}
