package com.newmedia.erxeslibrary.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Integration extends RealmObject{
    @PrimaryKey
    private long _id;
    private String languageCode;
    private String uiOptions;
    private String messengerData;
}
