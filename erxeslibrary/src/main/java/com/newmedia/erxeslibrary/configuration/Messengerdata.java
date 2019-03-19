package com.newmedia.erxeslibrary.configuration;

import java.util.List;

public class Messengerdata {
    public boolean isOnline;
    public String timezone;
    public List<String> supporterIds;
    public String knowledgeBaseTopicId;
    public String availabilityMethod;


//    public Map<String, Messages> messages;
    public Messages messages;
    public class Messages{
        public String welcome,away,thank;
        public Greetings greetings;
    }
    public class Greetings{
        public String message,title;
    }
    public String getWelcome(String lan){
        if(messages==null || messages.welcome == null)
            return "";
        return messages.welcome;
    }
}
