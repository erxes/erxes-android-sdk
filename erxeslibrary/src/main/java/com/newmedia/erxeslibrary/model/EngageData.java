package com.newmedia.erxeslibrary.model;

import io.realm.RealmObject;

public class EngageData extends RealmObject {
    private String messageId;
    private String brandId;
    private String content;
    private String fromUserId;
    private User fromUser;
    private String kind;
    private String sentAs;
}
