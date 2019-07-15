package com.newmedia.erxeslibrary.model;

import com.newmedia.erxes.basic.FaqGetQuery;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseTopic {
    public String _id;
    public String title;
    public String description;
    public List<KnowledgeBaseCategory> categories;
    public String color;
    public String languageCode;

    public static KnowledgeBaseTopic convert(FaqGetQuery.Data data){
        KnowledgeBaseTopic knowledgeBaseTopic = new KnowledgeBaseTopic();
        knowledgeBaseTopic._id = data.knowledgeBaseTopicsDetail()._id();
        knowledgeBaseTopic.title = data.knowledgeBaseTopicsDetail().title();
        knowledgeBaseTopic.description = data.knowledgeBaseTopicsDetail().description();
        knowledgeBaseTopic.color = data.knowledgeBaseTopicsDetail().color();
        knowledgeBaseTopic.languageCode = data.knowledgeBaseTopicsDetail().languageCode();
        knowledgeBaseTopic.categories = new ArrayList<>();
        knowledgeBaseTopic.categories.addAll( KnowledgeBaseCategory.convert(data.knowledgeBaseTopicsDetail().categories()));
        return knowledgeBaseTopic;
    }
}
