package com.newmedia.erxeslibrary.Configuration;

// Adding a single ENUM will increase the size (13x times than the Integer constant) of final DEX file.
// It also generates the problem of runtime overhead and your app will required more space.
// don't use enum type
public class ReturnType {

    final static public int SERVERERROR = 0;
    final static public int CONNECTIONFAILED = 1;
    final static public int INTEGRATION_CHANGED = 2;
    final static public int LOGIN_SUCCESS = 3;
    final static public int Mutation = 4;
    final static public int Mutation_new = 5;
    final static public int Subscription = 6;
    final static public int Getmessages = 7;
    final static public int Getconversation = 8;
    final static public int IsMessengerOnline = 9;
    final static public int GetSupporters = 10;
}
