package com.newmedia.erxeslibrary.Configuration;

import com.newmedia.erxeslibrary.Model.Conversation;
import com.newmedia.erxeslibrary.Model.ConversationMessage;
import com.newmedia.erxeslibrary.Model.EngageData;
import com.newmedia.erxeslibrary.Model.Integration;
import com.newmedia.erxeslibrary.Model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.Model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.Model.KnowledgeBaseLoader;
import com.newmedia.erxeslibrary.Model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.Model.Supporter;
import com.newmedia.erxeslibrary.Model.User;

import io.realm.annotations.RealmModule;

@RealmModule(classes = {Conversation.class, ConversationMessage.class,
        EngageData.class, Integration.class, User.class, KnowledgeBaseArticle.class, KnowledgeBaseCategory.class,
        KnowledgeBaseLoader.class, KnowledgeBaseTopic.class,Supporter.class})
public class ErxesRealmModule {
}
