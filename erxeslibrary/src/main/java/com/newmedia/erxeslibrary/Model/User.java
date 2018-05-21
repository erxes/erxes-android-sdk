package com.newmedia.erxeslibrary.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject{
    @PrimaryKey
    public long _id;
    public UserDetails details;
}
