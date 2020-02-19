package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.KnowledgeBaseTopicDetailQuery;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseCategory {
    public String id;
    public String title;
    public String description;
    public int numOfArticles;
    public List<User> authors;
    public List<KnowledgeBaseArticle> articles;
    public String icon;
    static public List<KnowledgeBaseCategory> convert(List<KnowledgeBaseTopicDetailQuery.Category> itemuser){
        KnowledgeBaseCategory temp;
        List<KnowledgeBaseCategory> categories = new ArrayList<>();
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new KnowledgeBaseCategory();
            temp.id = itemuser.get(i)._id();
            temp.title = itemuser.get(i).title();
            temp.icon = itemuser.get(i).icon();
            temp.description = itemuser.get(i).description();
            temp.numOfArticles = itemuser.get(i).numOfArticles().intValue();
            temp.articles = new ArrayList<>();
            temp.articles.addAll(KnowledgeBaseArticle.convert(itemuser.get(i).articles()));
            categories.add(temp);
        }
        return categories;
    }
}
