package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.ConversationMessageInsertedSubscription;
import com.erxes.io.opens.WidgetsMessagesQuery;
import com.erxes.io.opens.WidgetsMessengerSupportersQuery;
import com.newmedia.erxeslibrary.helper.Json;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id;
    public String avatar;
    public String fullName;

    static public List<User> convert(List<WidgetsMessengerSupportersQuery.WidgetsMessengerSupporter> itemuser) {
        User temp;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < itemuser.size(); i++) {
            temp = new User();
            temp.id = itemuser.get(i)._id();
            temp.avatar = itemuser.get(i).details().avatar();
            temp.fullName = itemuser.get(i).details().fullName();
            users.add(temp);
        }
        return users;
    }
}
