package com.newmedia.erxeslibrary;

import com.newmedia.erxeslibrary.Configuration.ErrorType;

public interface ErxesObserver {
    void notify(boolean status, String conversationId, ErrorType errorType);
}
