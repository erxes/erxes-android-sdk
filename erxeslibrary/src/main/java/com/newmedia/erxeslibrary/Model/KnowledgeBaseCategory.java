package com.newmedia.erxeslibrary.Model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KnowledgeBaseCategory extends RealmObject {
    @PrimaryKey
    private long _id;
    private String title;
    private String description;
    private int numOfArticles;
    private RealmList<User> authors;
    private String icon;
}
