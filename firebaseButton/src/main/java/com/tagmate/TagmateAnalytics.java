package com.tagmate;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TagmateAnalytics {

    private static com.google.firebase.analytics.FirebaseAnalytics _instance;
    private static TagmateAnalytics fInstance;
    private final SharedPreferences sharedPreferences;
    //    private Bundle bundle;
    private final Utils utils;
    ExecutorService executorService;
    String appInstanceID;
    String deviceID;
    String packageName;
    boolean firstRun;
    String deviceName;

    String currentSessionId = "";

    private TagmateAnalytics(Context context) {
        Log.d("FIRE_BASE", "FirebaseAnalytics constructor called");

        utils = new Utils();

        sharedPreferences = context.getSharedPreferences("MY_PREF", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putBoolean("SERVER_STATUS", true);

        myEdit.commit();

        //deviceID
        deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("YOUR_APP_ID", "Settings: " + deviceID);

//        deviceName = Settings.Global.getString(context.getContentResolver(), "device_name");

        firstRun = sharedPreferences.getBoolean("firstRun", true);

        if (firstRun) {
            //DeviceId stored to clipBoard
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            String textToCopy = deviceID;
            ClipData clipData = ClipData.newPlainText("Device ID", textToCopy);
            clipboardManager.setPrimaryClip(clipData);
        }

        //packageName
        packageName = context.getApplicationContext().getPackageName();
        Log.d("MY_PACKAGE", "FirebaseAnalytics: " + packageName);

        //appInstanceId
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(context).getAppInstanceId().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    // Instance ID token
                    appInstanceID = task.getResult();
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    myEdit.putString("APP_INSTANCE_ID", appInstanceID);

                    myEdit.commit();
                    // Use the token as needed
                } else {
                    // Handle error
                }
            }
        });

        Log.d("ABC_XYZ", "FIREBASE_SP: " + sharedPreferences.getBoolean("SERVER_STATUS", false));

        //Bifurcate SERVER_STATUS
        _instance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);

        /*if (sharedPreferences.getBoolean("SERVER_STATUS", false)) {
            //send data to tatvic server
            //TODO: get bundle data
            _instance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);

        } else {
            //not subscribed -> data send to the GA
            _instance = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context);
        }*/

    }

    public static TagmateAnalytics getInstance(Context context) {
        if (fInstance == null) {
            //this will invoke constructor
            fInstance = new TagmateAnalytics(context);
        }
        return fInstance;
    }

    public void logEvent(String eventName, Bundle bundle) {
//        bundle = bundle;

//        _instance.logEvent(eventName,bundle);


        if (sharedPreferences.getBoolean("SERVER_STATUS", false)) {

            //TODO: send event to GA
            _instance.logEvent(eventName, bundle);

            //add code here
            executorService = Executors.newSingleThreadExecutor();

            final String[] response = {""};

            Bundle bundle2 = new Bundle();
            bundle2.putString("app_instance_id", sharedPreferences.getString("APP_INSTANCE_ID", ""));
            bundle2.putString("deviceId", deviceID);
            bundle2.putString("app_package_name", packageName);


                executorService.submit(new Runnable() {
                    @Override
                    public void run() {

                        //DeviceId and PackageName
                        try {

                            String modelName = android.os.Build.MODEL;
                            deviceName = android.os.Build.MANUFACTURER;

                            Bundle b3 = new Bundle();
                            b3.putString("packageName", packageName);
                            b3.putString("deviceId", deviceID);
                            b3.putString("modelName", deviceName);
                            b3.putString("modelNumber", modelName);

//                        String json = "{\"event_name\":" + "\"" + eventName + "\"" + "," + "\"params\":" + utils.bundleToJsonString3(bundle) + "," + "\"meta\":" + utils.bundleToJsonString3(bundle2) + "}";
                            String json = utils.bundleToJsonString3(b3);
                            Log.d("OUR_JSON_RES_PACKAGE_B3", json);

                            // Step 4: Make the POST network call
//                        String urlEndpoint = "https://debugger-dev.tagmate.app/api/v1/debugger/appRequests/check/device";
                            String urlEndpoint = "http://192.168.0.218:3050/api/v1/debugger/appRequests/check/device";
                            URL url = new URL(urlEndpoint);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setDoOutput(true);

                            try (OutputStream outputStream = connection.getOutputStream()) {
                                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                                outputStream.write(input, 0, input.length);
                            }

                            int responseCode = connection.getResponseCode();
                            // Process the response as needed

                            if (responseCode == 200) {
                                try (InputStream inputStream = connection.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
//                                Log.d("RES_CODE_HERE_RES_PACKAGE", bufferedReader.readLine() + " ");
                                    response[0] = bufferedReader.readLine();
                                    Log.d("RES_0", "run: " + response[0]);

                                    String jsonString = response[0];

                                    // Create a JSONObject from the JSON string
                                    JSONObject jsonObject = new JSONObject(jsonString);

                                    // Read the value of the "success" key
                                    String status = jsonObject.getString("status");
                                    Log.d("OUR_STATUS", "run: " + status);

                                    if (status.equals("SUCCESS")) {
                                        JSONObject jsonData = jsonObject.getJSONObject("data");
                                        Log.d("OUR_STATUS", "run: " + jsonData);

                                        currentSessionId = jsonData.getString("sessionId");
                                        Log.d("OUR_STATUS", "run: " + currentSessionId);


                                    }

                                }
                            }

                            if (responseCode == 500) {
                                Log.d("MAKE_ERROR", "App's package name is not registerd with tagmate");
                            }

                            Log.d("YOUR_RES_CODE_PACKAGE", "responseCode: " + responseCode);

                            connection.disconnect();

                        } catch (Exception e) {
                            Log.d("YOUR_RES_CATCH", "error: " + e.getMessage());
                            e.printStackTrace();
                        }


                        try {
                            if (!currentSessionId.isEmpty() && !currentSessionId.equals("") && !currentSessionId.contains("null")) {
                                String json = "{\"event_name\":" + "\"" + eventName + "\"" + "," + "\"params\":" + utils.bundleToJsonString3(bundle) + "," + "\"meta\":" + utils.bundleToJsonString3(bundle2) + "}";

                                Log.d("OUR_JSON_RES", json);

                                // Step 4: Make the POST network call
//                        String urlEndpoint = "https://debugger-dev.tagmate.app/api/v1/debugger/appRequests";
                                String urlEndpoint = "http://192.168.0.218:3050/api/v1/debugger/appRequests";
                                URL url = new URL(urlEndpoint);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/json");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setDoOutput(true);

                                try (OutputStream outputStream = connection.getOutputStream()) {
                                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                                    outputStream.write(input, 0, input.length);
                                }

                                int responseCode = connection.getResponseCode();
                                // Process the response as needed

                                if (responseCode == 200) {
                                    try (InputStream inputStream = connection.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                                        Log.d("RES_CODE_HERE_RES", bufferedReader.readLine() + " ");
                                        response[0] = bufferedReader.readLine();
                               /* String jsonString = response[0];

                                // Create a JSONObject from the JSON string
                                JSONObject jsonObject = new JSONObject(jsonString);

                                // Read the value of the "success" key
                                String status = jsonObject.getString("status");
                                Log.d("OUR_STATUS", "run: "+status);*/
                                    }
                                }

                                Log.d("YOUR_RES_CODE", "responseCode: " + responseCode);

                                connection.disconnect();
                            }
                            else {
                                Log.d("CHECK_SESSION", "session is getting null ");
                            }
                        } catch (Exception e) {
                            Log.d("YOUR_RES_C", "error: " + e.getMessage());
                            e.printStackTrace();
                        }

                 /*       //DeviceId and PackageName
                        try {

                            String modelName = android.os.Build.MODEL;
                            deviceName = android.os.Build.MANUFACTURER;

                            Bundle b3 = new Bundle();
                            b3.putString("packageName", packageName);
                            b3.putString("deviceId", deviceID);
                            b3.putString("modelName", deviceName);
                            b3.putString("modelNumber", modelName);

//                        String json = "{\"event_name\":" + "\"" + eventName + "\"" + "," + "\"params\":" + utils.bundleToJsonString3(bundle) + "," + "\"meta\":" + utils.bundleToJsonString3(bundle2) + "}";
                            String json = utils.bundleToJsonString3(b3);
                            Log.d("OUR_JSON_RES_PACKAGE_B3", json);

                            // Step 4: Make the POST network call
//                        String urlEndpoint = "https://debugger-dev.tagmate.app/api/v1/debugger/appRequests/check/device";
                            String urlEndpoint = "http://192.168.0.218:3050/api/v1/debugger/appRequests/check/device";
                            URL url = new URL(urlEndpoint);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setDoOutput(true);

                            try (OutputStream outputStream = connection.getOutputStream()) {
                                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                                outputStream.write(input, 0, input.length);
                            }

                            int responseCode = connection.getResponseCode();
                            // Process the response as needed

                            if (responseCode == 200) {
                                try (InputStream inputStream = connection.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
//                                Log.d("RES_CODE_HERE_RES_PACKAGE", bufferedReader.readLine() + " ");
                                    response[0] = bufferedReader.readLine();
                                    Log.d("RES_0", "run: " + response[0]);

                                    String jsonString = response[0];

                                    // Create a JSONObject from the JSON string
                                    JSONObject jsonObject = new JSONObject(jsonString);

                                    // Read the value of the "success" key
                                    String status = jsonObject.getString("status");
                                    Log.d("OUR_STATUS", "run: " + status);

                                    if (status.equals("SUCCESS")) {
                                        JSONObject jsonData = jsonObject.getJSONObject("data");
                                        Log.d("OUR_STATUS", "run: " + jsonData);

                                        currentSessionId = jsonData.getString("sessionId");
                                        Log.d("OUR_STATUS", "run: " + currentSessionId);


                                    }

                                }
                            }

                            if (responseCode == 500) {
                                Log.d("MAKE_ERROR", "App's package name is not registerd with tagmate");
                            }

                            Log.d("YOUR_RES_CODE_PACKAGE", "responseCode: " + responseCode);

                            connection.disconnect();

                        } catch (Exception e) {
                            Log.d("YOUR_RES_CATCH", "error: " + e.getMessage());
                            e.printStackTrace();
                        }*/

                    }
                });

        } else {
            _instance.logEvent(eventName, bundle);
        }

    }

    public void setUserProperty(String name, String value) {
        _instance.setUserProperty(name, value);
    }

    //set current scree/ default param

    public void setUserId(@Nullable String id) {
        _instance.setUserId(id);
    }

    public void setSessionTimeoutDuration(long milliseconds) {
        _instance.setSessionTimeoutDuration(milliseconds);
    }

    public void setDefaultEventParameters(@Nullable Bundle parameters) {
        _instance.setDefaultEventParameters(parameters);
    }

    public void setCurrentScreen(@NonNull Activity activity, @Nullable @Size(min = 1L, max = 36L) String screenName, @Nullable @Size(min = 1L, max = 36L) String screenClassOverride) {
        _instance.setCurrentScreen(activity, screenName, screenClassOverride);
    }

    public void setConsent(@NonNull Map<com.google.firebase.analytics.FirebaseAnalytics.ConsentType, com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus> consentSettings) {
        _instance.setConsent(consentSettings);
    }

    public void setAnalyticsCollectionEnabled(boolean enabled) {
        _instance.setAnalyticsCollectionEnabled(enabled);
    }

    public void resetAnalyticsData() {
        _instance.resetAnalyticsData();
    }

    public String getFirebaseInstanceId() {
        return _instance.getFirebaseInstanceId();
    }

    public Task<Long> getSessionId() {
        return _instance.getSessionId();
    }

    public Task<String> getAppInstanceId() {
        return _instance.getAppInstanceId();
    }

    public static class Event {
        @NonNull
        public static final String AD_IMPRESSION = com.google.firebase.analytics.FirebaseAnalytics.Event.AD_IMPRESSION;
        @NonNull
        public static final String ADD_PAYMENT_INFO = com.google.firebase.analytics.FirebaseAnalytics.Event.ADD_PAYMENT_INFO;
        @NonNull
        public static final String ADD_TO_CART = com.google.firebase.analytics.FirebaseAnalytics.Event.ADD_TO_CART;
        @NonNull
        public static final String ADD_TO_WISHLIST = com.google.firebase.analytics.FirebaseAnalytics.Event.ADD_TO_WISHLIST;
        @NonNull
        public static final String APP_OPEN = com.google.firebase.analytics.FirebaseAnalytics.Event.APP_OPEN;
        @NonNull
        public static final String BEGIN_CHECKOUT = com.google.firebase.analytics.FirebaseAnalytics.Event.BEGIN_CHECKOUT;
        @NonNull
        public static final String CAMPAIGN_DETAILS = com.google.firebase.analytics.FirebaseAnalytics.Event.CAMPAIGN_DETAILS;
        @NonNull
        public static final String GENERATE_LEAD = com.google.firebase.analytics.FirebaseAnalytics.Event.GENERATE_LEAD;
        @NonNull
        public static final String JOIN_GROUP = com.google.firebase.analytics.FirebaseAnalytics.Event.JOIN_GROUP;
        @NonNull
        public static final String LEVEL_END = com.google.firebase.analytics.FirebaseAnalytics.Event.LEVEL_END;
        @NonNull
        public static final String LEVEL_START = com.google.firebase.analytics.FirebaseAnalytics.Event.LEVEL_START;
        @NonNull
        public static final String LEVEL_UP = com.google.firebase.analytics.FirebaseAnalytics.Event.LEVEL_UP;
        @NonNull
        public static final String LOGIN = com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN;
        @NonNull
        public static final String POST_SCORE = com.google.firebase.analytics.FirebaseAnalytics.Event.POST_SCORE;
        @NonNull
        public static final String SEARCH = com.google.firebase.analytics.FirebaseAnalytics.Event.SEARCH;
        @NonNull
        public static final String SELECT_CONTENT = com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT;
        @NonNull
        public static final String SHARE = com.google.firebase.analytics.FirebaseAnalytics.Event.SHARE;
        @NonNull
        public static final String SIGN_UP = com.google.firebase.analytics.FirebaseAnalytics.Event.SIGN_UP;
        @NonNull
        public static final String SPEND_VIRTUAL_CURRENCY = com.google.firebase.analytics.FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY;
        @NonNull
        public static final String TUTORIAL_BEGIN = com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_BEGIN;
        @NonNull
        public static final String TUTORIAL_COMPLETE = com.google.firebase.analytics.FirebaseAnalytics.Event.TUTORIAL_COMPLETE;
        @NonNull
        public static final String UNLOCK_ACHIEVEMENT = com.google.firebase.analytics.FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT;
        @NonNull
        public static final String VIEW_ITEM = com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_ITEM;
        @NonNull
        public static final String VIEW_ITEM_LIST = com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_ITEM_LIST;
        @NonNull
        public static final String VIEW_SEARCH_RESULTS = com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS;
        @NonNull
        public static final String EARN_VIRTUAL_CURRENCY = com.google.firebase.analytics.FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY;
        @NonNull
        public static final String SCREEN_VIEW = com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW;
        @NonNull
        public static final String REMOVE_FROM_CART = com.google.firebase.analytics.FirebaseAnalytics.Event.REMOVE_FROM_CART;
        @NonNull
        public static final String ADD_SHIPPING_INFO = com.google.firebase.analytics.FirebaseAnalytics.Event.ADD_SHIPPING_INFO;
        @NonNull
        public static final String PURCHASE = com.google.firebase.analytics.FirebaseAnalytics.Event.PURCHASE;
        @NonNull
        public static final String REFUND = com.google.firebase.analytics.FirebaseAnalytics.Event.REFUND;
        @NonNull
        public static final String SELECT_ITEM = com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_ITEM;
        @NonNull
        public static final String SELECT_PROMOTION = com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_PROMOTION;
        @NonNull
        public static final String VIEW_CART = com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_CART;
        @NonNull
        public static final String VIEW_PROMOTION = com.google.firebase.analytics.FirebaseAnalytics.Event.VIEW_PROMOTION;

        protected Event() {
        }
    }

    public static class Param {
        @NonNull
        public static final String ACHIEVEMENT_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.ACHIEVEMENT_ID;
        @NonNull
        public static final String AD_FORMAT = com.google.firebase.analytics.FirebaseAnalytics.Param.AD_FORMAT;
        @NonNull
        public static final String AD_PLATFORM = com.google.firebase.analytics.FirebaseAnalytics.Param.AD_PLATFORM;
        @NonNull
        public static final String AD_SOURCE = com.google.firebase.analytics.FirebaseAnalytics.Param.AD_SOURCE;
        @NonNull
        public static final String AD_UNIT_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.AD_UNIT_NAME;
        @NonNull
        public static final String CHARACTER = com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER;
        @NonNull
        public static final String TRAVEL_CLASS = com.google.firebase.analytics.FirebaseAnalytics.Param.TRAVEL_CLASS;
        @NonNull
        public static final String CONTENT_TYPE = com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE;
        @NonNull
        public static final String CURRENCY = com.google.firebase.analytics.FirebaseAnalytics.Param.CURRENCY;
        @NonNull
        public static final String COUPON = com.google.firebase.analytics.FirebaseAnalytics.Param.COUPON;
        @NonNull
        public static final String START_DATE = com.google.firebase.analytics.FirebaseAnalytics.Param.START_DATE;
        @NonNull
        public static final String END_DATE = com.google.firebase.analytics.FirebaseAnalytics.Param.END_DATE;
        @NonNull
        public static final String EXTEND_SESSION = com.google.firebase.analytics.FirebaseAnalytics.Param.EXTEND_SESSION;
        @NonNull
        public static final String FLIGHT_NUMBER = com.google.firebase.analytics.FirebaseAnalytics.Param.FLIGHT_NUMBER;
        @NonNull
        public static final String GROUP_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.GROUP_ID;
        @NonNull
        public static final String ITEM_CATEGORY = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY;
        @NonNull
        public static final String ITEM_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID;
        @NonNull
        public static final String ITEM_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME;
        @NonNull
        public static final String LOCATION = com.google.firebase.analytics.FirebaseAnalytics.Param.LOCATION;
        @NonNull
        public static final String LEVEL = com.google.firebase.analytics.FirebaseAnalytics.Param.LEVEL;
        @NonNull
        public static final String LEVEL_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.LEVEL_NAME;
        @NonNull
        public static final String METHOD = com.google.firebase.analytics.FirebaseAnalytics.Param.METHOD;
        @NonNull
        public static final String NUMBER_OF_NIGHTS = com.google.firebase.analytics.FirebaseAnalytics.Param.NUMBER_OF_NIGHTS;
        @NonNull
        public static final String NUMBER_OF_PASSENGERS = com.google.firebase.analytics.FirebaseAnalytics.Param.NUMBER_OF_PASSENGERS;
        @NonNull
        public static final String NUMBER_OF_ROOMS = com.google.firebase.analytics.FirebaseAnalytics.Param.NUMBER_OF_ROOMS;
        @NonNull
        public static final String DESTINATION = com.google.firebase.analytics.FirebaseAnalytics.Param.DESTINATION;
        @NonNull
        public static final String ORIGIN = com.google.firebase.analytics.FirebaseAnalytics.Param.ORIGIN;
        @NonNull
        public static final String PRICE = com.google.firebase.analytics.FirebaseAnalytics.Param.PRICE;
        @NonNull
        public static final String QUANTITY = com.google.firebase.analytics.FirebaseAnalytics.Param.QUANTITY;
        @NonNull
        public static final String SCORE = com.google.firebase.analytics.FirebaseAnalytics.Param.SCORE;
        @NonNull
        public static final String SHIPPING = com.google.firebase.analytics.FirebaseAnalytics.Param.SHIPPING;
        @NonNull
        public static final String TRANSACTION_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.TRANSACTION_ID;
        @NonNull
        public static final String SEARCH_TERM = com.google.firebase.analytics.FirebaseAnalytics.Param.SEARCH_TERM;
        @NonNull
        public static final String SUCCESS = com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS;
        @NonNull
        public static final String TAX = com.google.firebase.analytics.FirebaseAnalytics.Param.TAX;
        @NonNull
        public static final String VALUE = com.google.firebase.analytics.FirebaseAnalytics.Param.VALUE;
        @NonNull
        public static final String VIRTUAL_CURRENCY_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME;
        @NonNull
        public static final String CAMPAIGN = com.google.firebase.analytics.FirebaseAnalytics.Param.CAMPAIGN;
        @NonNull
        public static final String SOURCE = com.google.firebase.analytics.FirebaseAnalytics.Param.SOURCE;
        @NonNull
        public static final String MEDIUM = com.google.firebase.analytics.FirebaseAnalytics.Param.MEDIUM;
        @NonNull
        public static final String TERM = com.google.firebase.analytics.FirebaseAnalytics.Param.TERM;
        @NonNull
        public static final String CONTENT = com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT;
        @NonNull
        public static final String ACLID = com.google.firebase.analytics.FirebaseAnalytics.Param.ACLID;
        @NonNull
        public static final String CP1 = com.google.firebase.analytics.FirebaseAnalytics.Param.CP1;
        @NonNull
        public static final String ITEM_BRAND = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_BRAND;
        @NonNull
        public static final String ITEM_VARIANT = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_VARIANT;
        @NonNull
        public static final String CREATIVE_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.CREATIVE_NAME;
        @NonNull
        public static final String CREATIVE_SLOT = com.google.firebase.analytics.FirebaseAnalytics.Param.CREATIVE_SLOT;
        @NonNull
        public static final String AFFILIATION = com.google.firebase.analytics.FirebaseAnalytics.Param.AFFILIATION;
        @NonNull
        public static final String INDEX = com.google.firebase.analytics.FirebaseAnalytics.Param.INDEX;
        @NonNull
        public static final String DISCOUNT = com.google.firebase.analytics.FirebaseAnalytics.Param.DISCOUNT;
        @NonNull
        public static final String ITEM_CATEGORY2 = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY2;
        @NonNull
        public static final String ITEM_CATEGORY3 = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY3;
        @NonNull
        public static final String ITEM_CATEGORY4 = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY4;
        @NonNull
        public static final String ITEM_CATEGORY5 = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_CATEGORY5;
        @NonNull
        public static final String ITEM_LIST_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_LIST_ID;
        @NonNull
        public static final String ITEM_LIST_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_LIST_NAME;
        @NonNull
        public static final String ITEMS = com.google.firebase.analytics.FirebaseAnalytics.Param.ITEMS;
        @NonNull
        public static final String LOCATION_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.LOCATION_ID;
        @NonNull
        public static final String PAYMENT_TYPE = com.google.firebase.analytics.FirebaseAnalytics.Param.PAYMENT_TYPE;
        @NonNull
        public static final String PROMOTION_ID = com.google.firebase.analytics.FirebaseAnalytics.Param.PROMOTION_ID;
        @NonNull
        public static final String PROMOTION_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.PROMOTION_NAME;
        @NonNull
        public static final String SCREEN_CLASS = com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS;
        @NonNull
        public static final String SCREEN_NAME = com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME;
        @NonNull
        public static final String SHIPPING_TIER = com.google.firebase.analytics.FirebaseAnalytics.Param.SHIPPING_TIER;

        protected Param() {
        }
    }

    public static class UserProperty {
        @NonNull
        public static final String SIGN_UP_METHOD = com.google.firebase.analytics.FirebaseAnalytics.UserProperty.SIGN_UP_METHOD;
        @NonNull
        public static final String ALLOW_AD_PERSONALIZATION_SIGNALS = com.google.firebase.analytics.FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS;

        protected UserProperty() {
        }
    }
}