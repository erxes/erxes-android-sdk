package com.newmedia.erxeslibrary.model;

import android.util.Log;

import com.newmedia.erxes.basic.FaqGetQuery;
import com.newmedia.erxes.basic.MessengerSupportersQuery;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class KnowledgeBaseCategory extends RealmObject {
    @PrimaryKey
    public String _id;
    public String title;
    public String description;
    public int numOfArticles;
    public RealmList<User> authors;
    public String icon;
    static public List<KnowledgeBaseCategory> convert(List<FaqGetQuery.Category> itemuser){
        KnowledgeBaseCategory temp;
        List<KnowledgeBaseCategory> categories = new ArrayList<>();
        Log.d("xaxa","size is "+itemuser.size());
        for(int  i = 0 ; i <  itemuser.size(); i++ ) {
            temp = new KnowledgeBaseCategory();
            temp._id = itemuser.get(i)._id();
            temp.title = itemuser.get(i).title();
            temp.description = itemuser.get(i).description();
            categories.add(temp);
        }
        return categories;
    }
}
