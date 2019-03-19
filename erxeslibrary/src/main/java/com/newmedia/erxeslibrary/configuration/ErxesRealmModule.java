package com.newmedia.erxeslibrary.configuration;

import com.newmedia.erxeslibrary.model.Conversation;
import com.newmedia.erxeslibrary.model.ConversationMessage;
import com.newmedia.erxeslibrary.model.EngageData;
import com.newmedia.erxeslibrary.model.Integration;
import com.newmedia.erxeslibrary.model.KnowledgeBaseArticle;
import com.newmedia.erxeslibrary.model.KnowledgeBaseCategory;
import com.newmedia.erxeslibrary.model.KnowledgeBaseLoader;
import com.newmedia.erxeslibrary.model.KnowledgeBaseTopic;
import com.newmedia.erxeslibrary.model.Supporter;
import com.newmedia.erxeslibrary.model.User;

import io.realm.annotations.RealmModule;

@RealmModule(library = true,classes = {Conversation.class, ConversationMessage.class,
        EngageData.class, Integration.class, User.class, KnowledgeBaseArticle.class, KnowledgeBaseCategory.class,
        KnowledgeBaseLoader.class, KnowledgeBaseTopic.class,Supporter.class})
public class ErxesRealmModule {
}
