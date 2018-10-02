package com.newmedia.erxeslibrary.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KnowledgeBaseTopic extends RealmObject {
    @PrimaryKey
    private long _id;
    private String title;
    private String description;
    private RealmList<KnowledgeBaseCategory> categories;
    private String color;
    private String languageCode;
}
