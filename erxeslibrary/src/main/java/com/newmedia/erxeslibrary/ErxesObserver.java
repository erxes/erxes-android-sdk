package com.newmedia.erxeslibrary;

import com.newmedia.erxeslibrary.Configuration.ReturnType;

public interface ErxesObserver {
    void notify(ReturnType returnType, String conversationId, String message);
}
