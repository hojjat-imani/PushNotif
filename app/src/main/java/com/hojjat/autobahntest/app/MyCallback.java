package com.hojjat.autobahntest.app;

import android.os.Parcel;
import android.os.Parcelable;

import com.hojjat.autobahntest.befrest.BefrestCallbacks;

/**
 * Created by ehsan on 11/10/2015.
 */
public class MyCallback implements BefrestCallbacks, Parcelable {
    @Override
    public void onTextMessageRecieved(String message) {

    }

    @Override
    public void onAuthenticationError(String errMsg) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
