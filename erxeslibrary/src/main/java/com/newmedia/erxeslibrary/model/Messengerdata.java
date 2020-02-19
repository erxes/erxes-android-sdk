package com.newmedia.erxeslibrary.model;

import com.newmedia.erxeslibrary.helper.Json;

import java.util.List;
import java.util.Map;

public class Messengerdata {
    private boolean isOnline, requireAuth, showChat, showLauncher, forceLogoutWhenResolve;
    private String timezone;
    private List<String> supporterIds;
    private String knowledgeBaseTopicId;
    private String availabilityMethod;
    private String formCode;
    private String facebook, twitter, youtube;
    private Messages messages;

    public static Messengerdata convert(Json jsonObject, String languageCode) {
        Messengerdata messengerdata = new Messengerdata();
        Map data = jsonObject.object;
        if (data.containsKey("isOnline") && data.get("isOnline") != null) {
            messengerdata.setOnline((Boolean) data.get("isOnline"));
        }
        if (data.containsKey("timezone"))
            messengerdata.setTimezone((String) data.get("timezone"));
//        if (data.containsKey("supporterIds")) {
//            List<Map> supporterIds = (List<Map>) data.get("supporterIds");
//            List<String> supIds = new ArrayList<>();
//            if (supporterIds != null) {
//                for (int i = 0; i < supporterIds.size(); i++) {
//                    supIds.add(supporterIds.get(i).toString());
//                }
//            }
//            messengerdata.setSupporterIds(supIds);
//        }
        if (data.containsKey("requireAuth"))
            messengerdata.setRequireAuth((Boolean) data.get("requireAuth"));
        if (data.containsKey("showChat"))
            messengerdata.setShowChat((Boolean) data.get("showChat"));
        if (data.containsKey("showLauncher"))
            messengerdata.setShowLauncher((Boolean) data.get("showLauncher"));
        if (data.containsKey("forceLogoutWhenResolve"))
            messengerdata.setForceLogoutWhenResolve((Boolean) data.get("forceLogoutWhenResolve"));

        if (data.containsKey("knowledgeBaseTopicId"))
            messengerdata.setKnowledgeBaseTopicId((String) data.get("knowledgeBaseTopicId"));
        if (data.containsKey("availabilityMethod"))
            messengerdata.setAvailabilityMethod((String) data.get("availabilityMethod"));
        if (data.containsKey("formCode"))
            messengerdata.setFormCode((String) data.get("formCode"));
        if (data.containsKey("links")) {
            Map links = (Map) data.get("links");
            if (links.containsKey("facebook"))
                messengerdata.setFacebook((String) links.get("facebook"));
            if (links.containsKey("twitter"))
                messengerdata.setTwitter((String) links.get("twitter"));
            if (links.containsKey("youtube"))
                messengerdata.setYoutube((String) links.get("youtube"));
        }
        if (data.containsKey("messages")) {
            Map messageJson = (Map) data.get("messages");
            if (languageCode != null && messageJson.containsKey(languageCode)) {
                Map lanJson = (Map) messageJson.get(languageCode);
                Messages messages = new Messages();
                if (lanJson.containsKey("welcome"))
                    messages.setWelcome((String) lanJson.get("welcome"));
                if (lanJson.containsKey("away"))
                    messages.setAway((String) lanJson.get("away"));
                if (lanJson.containsKey("thank"))
                    messages.setThank((String) lanJson.get("thank"));
                if (lanJson.containsKey("greetings")) {
                    Greetings greetings = new Greetings();
                    Map greetingsJson = (Map) lanJson.get("greetings");

                    if (greetingsJson.containsKey("title"))
                        greetings.setTitle((String) greetingsJson.get("title"));
                    if (greetingsJson.containsKey("message"))
                        greetings.setMessage((String) greetingsJson.get("message"));
                    messages.setGreetings(greetings);
                }
                messengerdata.setMessages(messages);
            } else {
                Messages messages = new Messages();
                if (messageJson.containsKey("welcome"))
                    messages.setWelcome((String) messageJson.get("welcome"));
                if (messageJson.containsKey("away"))
                    messages.setAway((String) messageJson.get("away"));
                if (messageJson.containsKey("thank"))
                    messages.setThank((String) messageJson.get("thank"));
                if (messageJson.containsKey("greetings")) {
                    Greetings greetings = new Greetings();
                    Map greetingsJson = (Map) messageJson.get("greetings");

                    if (greetingsJson.containsKey("title"))
                        greetings.setTitle((String) greetingsJson.get("title"));
                    if (greetingsJson.containsKey("message"))
                        greetings.setMessage((String) greetingsJson.get("message"));
                    messages.setGreetings(greetings);
                }
                messengerdata.setMessages(messages);
            }
        }
        return messengerdata;
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
