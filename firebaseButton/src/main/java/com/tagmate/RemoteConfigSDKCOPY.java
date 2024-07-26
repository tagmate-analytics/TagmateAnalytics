//package com.tagmate;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//
//public class RemoteConfigSDK {
//    private static final String PREFS_NAME = "RemoteConfigPrefs";
//    private static final String BUTTON_TEXT_KEY = "button_text";
//    private static final String DEFAULT_BUTTON_TEXT = "Let's go";
//    private SharedPreferences sharedPreferences;
//
//    private static RemoteConfigSDK instance;
//
//    private RemoteConfigSDK(Context context){
//        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//    }
//
//    public static RemoteConfigSDK getInstance(Context context){
//        if (instance == null){
//            instance = new RemoteConfigSDK(context);
//        }
//        return instance;
//    }
//
//    public void fetchRemoteConfig() {
//        // Mocking remote fetch. In real implementation, make a network request here.
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(BUTTON_TEXT_KEY, DEFAULT_BUTTON_TEXT);
//        editor.apply();
//    }
//
//    public String getButtonText() {
//        return sharedPreferences.getString(BUTTON_TEXT_KEY, DEFAULT_BUTTON_TEXT);
//    }
//}