package com.newmedia.erxeslibrary.utils;

// Adding a single ENUM will increase the size (13x times than the Integer constant) of final DEX file.
// It also generates the problem of runtime overhead and your app will required more space.
// don't use enum type
public class ReturntypeUtil {
    public static final int SERVERERROR = 0;
    public static final int CONNECTIONFAILED = 1;
    public static final int LOGINSUCCESS = 2;
    public static final int MUTATION = 3;
    public static final int MUTATIONNEW = 4;
    public static final int SUBSCRIPTION = 5;
    public static final int GETMESSAGES = 6;
    public static final int GETCONVERSATION = 7;
    public static final int ISMESSENGERONLINE = 8;
    public static final int GETSUPPORTERS = 9;
    public static final int LEAD = 10;
    public static final int FAQ = 11;
    public static final int SAVEDLEAD = 12;
    public static final int COMINGNEWMESSAGE = 13;
    public static final int PROVIDER = 14;
}
