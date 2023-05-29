package com.dada.firebasebutton2.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class BaseApp extends Application {

    public SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean("SERVER_STATUS", true);

        myEdit.commit();
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

}
