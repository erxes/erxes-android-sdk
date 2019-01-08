package com.newmedia.erxeslibrary.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Supporter extends RealmObject {
    @PrimaryKey
    String _id;
    String avatar;
    String fullName;
}
