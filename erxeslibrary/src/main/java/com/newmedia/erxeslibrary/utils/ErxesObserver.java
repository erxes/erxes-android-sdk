package com.newmedia.erxeslibrary.utils;

public interface ErxesObserver {
    void notify(int returnType, String conversationId, String message);
}
