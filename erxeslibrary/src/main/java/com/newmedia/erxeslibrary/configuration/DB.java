package com.newmedia.erxeslibrary.configuration;

import com.newmedia.erxeslibrary.model.Conversation;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmModel;
import io.realm.RealmObject;

public class DB {

    final static public String database_name = "erxes.realm";
    final static public int database_version = 1;
    final static private RealmConfiguration myConfig = new RealmConfiguration.Builder()
            .name(database_name)
            .modules(new ErxesRealmModule())
            .schemaVersion(database_version)
            .deleteRealmIfMigrationNeeded()
            .build();
    static public Realm getDB(){
        return  Realm.getInstance(myConfig);
    }
    static public void save(RealmObject realmModel){
        Realm inner = Realm.getInstance(myConfig);
        inner.beginTransaction();
        inner.insertOrUpdate(realmModel);
        inner.commitTransaction();
        inner.close();
    }
    static public <E extends RealmModel> void save(Iterable<E>  list){
        Realm inner = Realm.getInstance(myConfig);
        inner.beginTransaction();
        inner.copyToRealmOrUpdate(list);
        inner.commitTransaction();
        inner.close();
    }

    static public Conversation getConversation(String id){
        Realm inner = Realm.getInstance(myConfig);
        Conversation a = inner.where(Conversation.class).equalTo("_id",id).findFirst();
        inner.close();
        return a;
    }


}
