package com.newmedia.erxeslibrary.model;

import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.basic.MessengerSupportersQuery;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id;
    public String avatar;
    public String fullName;

    public void convert(MessagesQuery.User itemuser){
        this.id = itemuser._id();
        this.avatar = itemuser.details().avatar();
        this.fullName = itemuser.details().fullName();
    }
    public void convert(ConversationMessageInsertedSubscription.User itemuser){
        this.id = itemuser._id();
        this.avatar = itemuser.details().avatar();
        this.fullName = itemuser.details().fullName();
    }
    static public List<User> convert(List<MessengerSupportersQuery.MessengerSupporter> itemuser){
        User temp;
        List<User> users = new ArrayList<>();
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new User();
            temp.id = itemuser.get(i)._id();
            temp.avatar = itemuser.get(i).details().avatar();
            temp.fullName = itemuser.get(i).details().fullName();
            users.add(temp);
        }
        return users;
    }
}
