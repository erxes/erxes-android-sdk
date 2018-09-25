package com.newmedia.erxeslibrary.Model;


//import com.newmedia.erxes.basic.GetSupporterQuery;
import com.newmedia.erxes.basic.MessagesQuery;
import com.newmedia.erxes.subscription.ConversationMessageInsertedSubscription;

import java.util.ArrayList;
import java.util.List;

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
//    static public List<User> convert(GetSupporterQuery.Data itemuser){
//        User temp;
//        List<User> users = new ArrayList<>();
//        for(int  i = 0 ; i <  itemuser.messengerSupporters().size(); i++ ) {
//            temp = new User();
//            temp._id = itemuser.messengerSupporters().get(i)._id();
//            temp.avatar = itemuser.messengerSupporters().get(i).details().avatar();
//            temp.fullName = itemuser.messengerSupporters().get(i).details().fullName();
//            users.add(temp);
//        }
//        return users;
//    }
}
