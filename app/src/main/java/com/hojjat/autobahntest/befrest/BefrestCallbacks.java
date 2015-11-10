package com.hojjat.autobahntest.befrest;

/**
 * Created by ehsan on 11/10/2015.
 */
public abstract class BefrestCallbacks {
    public abstract void onTextMessageRecieved(String message);
    public abstract void onAuthenticationError(String errMsg);
}