package com.newmedia.erxeslibrary;

public interface ErxesObserver {
    void notify(int returnType, String conversationId, String message);
}
