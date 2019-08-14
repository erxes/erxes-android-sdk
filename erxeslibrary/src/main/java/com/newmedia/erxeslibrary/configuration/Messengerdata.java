package com.newmedia.erxeslibrary.configuration;

import com.newmedia.erxeslibrary.helper.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Messengerdata {
    private boolean isOnline, requireAuth, showChat, showLauncher,forceLogoutWhenResolve;
    private String timezone;
    private List<String> supporterIds;
    private String knowledgeBaseTopicId;
    private String availabilityMethod;
    private String formCode;
    private String facebook, twitter, youtube;

    public static Messengerdata convert(Json jsonObject, String languageCode) {
        Messengerdata messengerdata = new Messengerdata();
        try {
            if (jsonObject.has("isOnline")) {
                messengerdata.setOnline(jsonObject.getBoolean("isOnline"));
            }
            if (jsonObject.has("timezone"))
                messengerdata.setTimezone(jsonObject.getString("timezone"));
            if (jsonObject.has("supporterIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("supporterIds");
                List<String> supIds = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    supIds.add(jsonArray.get(i).toString());
                }
                messengerdata.setSupporterIds(supIds);
            }
            if (jsonObject.has("requireAuth"))
                messengerdata.setRequireAuth(jsonObject.getBoolean("requireAuth"));
            if (jsonObject.has("showChat"))
                messengerdata.setShowChat(jsonObject.getBoolean("showChat"));
            if (jsonObject.has("showLauncher"))
                messengerdata.setShowLauncher(jsonObject.getBoolean("showLauncher"));
            if (jsonObject.has("forceLogoutWhenResolve"))
                messengerdata.setForceLogoutWhenResolve(jsonObject.getBoolean("forceLogoutWhenResolve"));

            if (jsonObject.has("knowledgeBaseTopicId"))
                messengerdata.setKnowledgeBaseTopicId(jsonObject.getString("knowledgeBaseTopicId"));
            if (jsonObject.has("availabilityMethod"))
                messengerdata.setAvailabilityMethod(jsonObject.getString("availabilityMethod"));
            if (jsonObject.has("formCode"))
                messengerdata.setFormCode(jsonObject.getString("formCode"));
            if (jsonObject.has("links")) {
                JSONObject links = jsonObject.getJSONObject("links");
                if (links.has("facebook"))
                    messengerdata.setFacebook(links.getString("facebook"));
                if (links.has("twitter"))
                    messengerdata.setTwitter(links.getString("twitter"));
                if (links.has("youtube"))
                    messengerdata.setYoutube(links.getString("youtube"));
            }
            if (jsonObject.has("messages")) {
                JSONObject messageJson = jsonObject.getJSONObject("messages");
                if (languageCode != null && messageJson.has(languageCode)) {
                    JSONObject lanJson = messageJson.getJSONObject(languageCode);
                    Messages messages = new Messages();
                    if (lanJson.has("welcome"))
                        messages.setWelcome(lanJson.getString("welcome"));
                    if (lanJson.has("away"))
                        messages.setAway(lanJson.getString("away"));
                    if (lanJson.has("thank"))
                        messages.setThank(lanJson.getString("thank"));
                    if (lanJson.has("greetings")) {
                        Greetings greetings = new Greetings();
                        JSONObject greetingsJson = lanJson.getJSONObject("greetings");

                        if (greetingsJson.has("title"))
                            greetings.setTitle(greetingsJson.getString("title"));
                        if (greetingsJson.has("message"))
                            greetings.setMessage(greetingsJson.getString("message"));
                        messages.setGreetings(greetings);
                    }
                    messengerdata.setMessages(messages);
                } else {
                    Messages messages = new Messages();
                    if (messageJson.has("welcome"))
                        messages.setWelcome(messageJson.getString("welcome"));
                    if (messageJson.has("away"))
                        messages.setAway(messageJson.getString("away"));
                    if (messageJson.has("thank"))
                        messages.setThank(messageJson.getString("thank"));
                    if (messageJson.has("greetings")) {
                        Greetings greetings = new Greetings();
                        JSONObject greetingsJson = messageJson.getJSONObject("greetings");

                        if (greetingsJson.has("title"))
                            greetings.setTitle(greetingsJson.getString("title"));
                        if (greetingsJson.has("message"))
                            greetings.setMessage(greetingsJson.getString("message"));
                        messages.setGreetings(greetings);
                    }
                    messengerdata.setMessages(messages);
                }
            }
            return messengerdata;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    public boolean isShowChat() {
        return showChat;
    }

    public void setShowChat(boolean showChat) {
        this.showChat = showChat;
    }

    public boolean isShowLauncher() {
        return showLauncher;
    }

    public void setShowLauncher(boolean showLauncher) {
        this.showLauncher = showLauncher;
    }

    public boolean isForceLogoutWhenResolve() {
        return forceLogoutWhenResolve;
    }

    public void setForceLogoutWhenResolve(boolean forceLogoutWhenResolve) {
        this.forceLogoutWhenResolve = forceLogoutWhenResolve;
    }

    //    public Map<String, Messages> messages;
    public Messages messages;

    public static class Messages {
        private String welcome, away, thank;
        private Greetings greetings;

        public String getWelcome() {
            return welcome;
        }

        public void setWelcome(String welcome) {
            this.welcome = welcome;
        }

        public String getAway() {
            return away;
        }

        public void setAway(String away) {
            this.away = away;
        }

        public String getThank() {
            return thank;
        }

        public void setThank(String thank) {
            this.thank = thank;
        }

        public Greetings getGreetings() {
            return greetings;
        }

        public void setGreetings(Greetings greetings) {
            this.greetings = greetings;
        }
    }

    public static class Greetings {
        private String message, title;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public String getWelcome(String lan) {
        if (getMessages() == null || getMessages().getWelcome() == null)
            return "";
        return getMessages().getWelcome();
    }

    public String getFormCode() {
        return formCode;
    }

    public void setFormCode(String formCode) {
        this.formCode = formCode;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<String> getSupporterIds() {
        return supporterIds;
    }

    public void setSupporterIds(List<String> supporterIds) {
        this.supporterIds = supporterIds;
    }

    public String getKnowledgeBaseTopicId() {
        return knowledgeBaseTopicId;
    }

    public void setKnowledgeBaseTopicId(String knowledgeBaseTopicId) {
        this.knowledgeBaseTopicId = knowledgeBaseTopicId;
    }

    public String getAvailabilityMethod() {
        return availabilityMethod;
    }

    public void setAvailabilityMethod(String availabilityMethod) {
        this.availabilityMethod = availabilityMethod;
    }

    public Messages getMessages() {
        return messages;
    }

    public void setMessages(Messages messages) {
        this.messages = messages;
    }
}
