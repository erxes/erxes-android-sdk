package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.KnowledgeBaseTopicDetailQuery;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseTopic {
    public String id;
    public String title;
    public String description;
    public List<KnowledgeBaseCategory> categories;
    public String color;
    public String languageCode;

    public static KnowledgeBaseTopic convert(KnowledgeBaseTopicDetailQuery.Data data){
        KnowledgeBaseTopic knowledgeBaseTopic = new KnowledgeBaseTopic();
        if (data.knowledgeBaseTopicDetail() != null) {
            knowledgeBaseTopic.id = data.knowledgeBaseTopicDetail()._id();
            knowledgeBaseTopic.title = data.knowledgeBaseTopicDetail().title();
            knowledgeBaseTopic.description = data.knowledgeBaseTopicDetail().description();
            knowledgeBaseTopic.color = data.knowledgeBaseTopicDetail().color();
            knowledgeBaseTopic.languageCode = data.knowledgeBaseTopicDetail().languageCode();
            knowledgeBaseTopic.categories = new ArrayList<>();
            knowledgeBaseTopic.categories.addAll(KnowledgeBaseCategory.convert(data.knowledgeBaseTopicDetail().categories()));
        }
        return knowledgeBaseTopic;
    }
}
