package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.ConversationMessageInsertedSubscription;
import com.erxes.io.opens.WidgetsMessagesQuery;
import com.erxes.io.opens.WidgetsMessengerSupportersQuery;
import com.newmedia.erxeslibrary.helper.Json;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String avatar;
    private String fullName;

    public static List<User> convert(List<WidgetsMessengerSupportersQuery.WidgetsMessengerSupporter> itemuser) {
        User temp;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < itemuser.size(); i++) {
            temp = new User();
            temp.id = itemuser.get(i)._id();
            if (itemuser.get(i).details() != null) {
                temp.avatar = itemuser.get(i).details().avatar();
                temp.fullName = itemuser.get(i).details().fullName();
            }
            users.add(temp);
        }
        return users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
