package com.newmedia.erxeslibrary.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KnowledgeBaseArticle extends RealmObject{
    @PrimaryKey
    private long _id;
    private String title;
    private String summary;
    private String content;
    private String createdBy;
    private long createdDate;
    private String modifiedBy;
    private long modifiedDate;
    private User author;
}
