package com.newmedia.erxeslibrary.model;

import android.util.Log;

import com.newmedia.erxes.basic.FaqGetQuery;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KnowledgeBaseTopic extends RealmObject {
    @PrimaryKey
    public String _id;
    public String title;
    public String description;
    public RealmList<KnowledgeBaseCategory> categories;
    public String color;
    public String languageCode;
    public void convert(FaqGetQuery.Data data){
        this._id = data.knowledgeBaseTopicsDetail()._id();
        this.title = data.knowledgeBaseTopicsDetail().title();
        this.description = data.knowledgeBaseTopicsDetail().description();
        this.color = data.knowledgeBaseTopicsDetail().color();
        this.languageCode = data.knowledgeBaseTopicsDetail().languageCode();
        this.categories = new RealmList<>();
        this.categories.addAll( KnowledgeBaseCategory.convert(data.knowledgeBaseTopicsDetail().categories()));
    }
}
