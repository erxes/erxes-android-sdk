package com.newmedia.erxeslibrary;

import com.newmedia.erxeslibrary.Configuration.ReturnType;

public interface ErxesObserver {
    void notify(int returnType, String conversationId, String message);
}
