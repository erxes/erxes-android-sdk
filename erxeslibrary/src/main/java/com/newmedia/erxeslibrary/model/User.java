package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.WidgetsConversationDetailQuery;
import com.erxes.io.opens.WidgetsMessengerSupportersQuery;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String avatar;
    private String fullName;
    private String shortName;
    private String position;
    private String description;

    public static List<User> convert(List<WidgetsMessengerSupportersQuery.Supporter> supporters) {
        User temp;
        List<User> users = new ArrayList<>();
        if (supporters != null)
            for (int i = 0; i < supporters.size(); i++) {
                temp = new User();
                temp.id = supporters.get(i)._id();
                if (supporters.get(i).details() != null) {
                    temp.avatar = supporters.get(i).details().avatar();
                    temp.fullName = supporters.get(i).details().fullName();
                    temp.shortName = supporters.get(i).details().shortName();
                    temp.description = supporters.get(i).details().description();
                    temp.position = supporters.get(i).details().position();
                }
                users.add(temp);
            }
        return users;
    }

    public static List<User> convertParticipatedUsers(List<WidgetsConversationDetailQuery.ParticipatedUser> participatedUsers) {
        User temp;
        List<User> users = new ArrayList<>();
        if (participatedUsers != null)
            for (int i = 0; i < participatedUsers.size(); i++) {
                temp = new User();
                temp.id = participatedUsers.get(i)._id();
                if (participatedUsers.get(i).details() != null) {
                    temp.avatar = participatedUsers.get(i).details().avatar();
                    temp.fullName = participatedUsers.get(i).details().fullName();
                    temp.shortName = participatedUsers.get(i).details().shortName();
                    temp.description = participatedUsers.get(i).details().description();
                    temp.position = participatedUsers.get(i).details().position();
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
