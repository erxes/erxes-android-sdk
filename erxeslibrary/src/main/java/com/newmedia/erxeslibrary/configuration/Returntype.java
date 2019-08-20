package com.newmedia.erxeslibrary.configuration;

// Adding a single ENUM will increase the size (13x times than the Integer constant) of final DEX file.
// It also generates the problem of runtime overhead and your app will required more space.
// don't use enum type
public class Returntype {

    public static final int SERVERERROR = 0;
    public static final int CONNECTIONFAILED = 1;
    public static final int INTEGRATIONCHANGED = 2;
    public static final int LOGINSUCCESS = 3;
    public static final int MUTATION = 4;
    public static final int MUTATIONNEW = 5;
    public static final int SUBSCRIPTION = 6;
    public static final int GETMESSAGES = 7;
    public static final int GETCONVERSATION = 8;
    public static final int ISMESSENGERONLINE = 9;
    public static final int GETSUPPORTERS = 10;
    public static final int LEAD = 11;
    public static final int FAQ = 12;
    public static final int SAVEDLEAD = 13;
    public static final int COMINGNEWMESSAGE = 14;
}
