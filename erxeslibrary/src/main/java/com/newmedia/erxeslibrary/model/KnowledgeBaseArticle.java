package com.newmedia.erxeslibrary.model;

import com.newmedia.erxes.basic.FaqGetQuery;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBaseArticle {
    public String _id;
    public String title;
    public String summary;
    public String content;
    public String createdBy;
    public String createdDate;
    public String modifiedBy;
    public String modifiedDate;
    public User author;

    static public List<KnowledgeBaseArticle> convert(List<FaqGetQuery.Article> itemuser){
        KnowledgeBaseArticle temp;
        List<KnowledgeBaseArticle> categories = new ArrayList<>();
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new KnowledgeBaseArticle();
            temp._id = itemuser.get(i)._id();
            temp.title = itemuser.get(i).title();
            temp.summary = itemuser.get(i).summary();
            temp.content = itemuser.get(i).content();
            temp.createdBy = itemuser.get(i).createdBy();
            temp.modifiedDate = itemuser.get(i).modifiedDate();
            temp.createdDate = itemuser.get(i).createdDate();
            categories.add(temp);
        }
        return categories;
    }
}
