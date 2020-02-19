package com.newmedia.erxeslibrary.model;

import com.erxes.io.opens.KnowledgeBaseTopicDetailQuery;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseArticle {
    public String id;
    public String title;
    public String summary;
    public String content;
    public String createdBy;
    public String createdDate;
    public String modifiedBy;
    public String modifiedDate;
    public User author;

    static public List<KnowledgeBaseArticle> convert(List<KnowledgeBaseTopicDetailQuery.Article> itemuser){
        KnowledgeBaseArticle temp;
        List<KnowledgeBaseArticle> categories = new ArrayList<>();
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new KnowledgeBaseArticle();
            temp.id = itemuser.get(i)._id();
            temp.title = itemuser.get(i).title();
            temp.summary = itemuser.get(i).summary();
            temp.content = itemuser.get(i).content();
            temp.createdBy = itemuser.get(i).createdBy();
            temp.modifiedDate = String.valueOf(itemuser.get(i).modifiedDate());
            temp.createdDate = String.valueOf(itemuser.get(i).createdDate());
            categories.add(temp);
        }
        return categories;
    }
}
