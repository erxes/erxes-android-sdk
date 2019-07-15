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
    public String _id;
    public String conversationId;
    public String customerId;
    public User user;
    public String content;
    public String createdAt;
    public boolean internal = false;
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
                JSONArray array = new JSONArray();
                for(int i = 0 ; i < item.attachments().size();i++){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("name",item.attachments().get(i).name());
                        json.put("size",item.attachments().get(i).size());
                        json.put("url",item.attachments().get(i).url());
                        json.put("type",item.attachments().get(i).type());
                        array.put(i,json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                this_o.attachments = array.toString();
            }

            this_o.conversationId = ConversationId;


            data_converted.add(this_o);
        }
        return data_converted;

    }
    static public ConversationMessage convert(InsertMessageMutation.InsertMessage a, String message,Config config){
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b._id = a._id();
        b.content = message;
        if(a.attachments()!=null) {
            JSONArray array = new JSONArray();
            for(int i = 0 ; i < a.attachments().size();i++){
                JSONObject json = new JSONObject();
                try {
                    json.put("name",a.attachments().get(i).name());
                    json.put("size",a.attachments().get(i).size());
                    json.put("url",a.attachments().get(i).url());
                    json.put("type",a.attachments().get(i).type());
                    array.put(i,json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            b.attachments = array.toString();
        }
        b.internal = false;
        b.customerId = config.customerId;//Config.customerId;
        return b;
    }
    static public ConversationMessage convert(ConversationMessageInsertedSubscription.ConversationMessageInserted a){
        ConversationMessage b = new ConversationMessage();
        b.conversationId = a.conversationId();
        b.createdAt = a.createdAt();
        b._id = a._id();
        b.content = a.content();
        if(a.attachments()!=null) {
            JSONArray array = new JSONArray();
            for(int i = 0 ; i < a.attachments().size();i++){
                JSONObject json = new JSONObject();
                try {
                    json.put("name",a.attachments().get(i).name());
                    json.put("size",a.attachments().get(i).size());
                    json.put("url",a.attachments().get(i).url());
                    json.put("type",a.attachments().get(i).type());
                    array.put(i,json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            b.attachments = array.toString();
        }
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
