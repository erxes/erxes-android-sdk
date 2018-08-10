package com.newmedia.erxeslibrary.Model;


import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject{
    @PrimaryKey
    public String _id;
    public String avatar;
    public String fullName;
    public void convert(MessagesQuery.User itemuser){
        this.avatar = itemuser.details().avatar();
        this.fullName = itemuser.details().fullName();
        this._id = itemuser._id();
    }
    public void convert(ConversationMessageInsertedSubscription.User itemuser){
        this.avatar = itemuser.details().avatar();
        this.fullName = itemuser.details().fullName();
        this._id = itemuser._id();
    }
}
