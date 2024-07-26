package com.tagmate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class RemoteConfigSDK {
    private static final String PREFS_NAME = "RemoteConfigPrefs";
    private static final String BUTTON_TEXT_KEY = "button_text";
    private static final String DEFAULT_BUTTON_TEXT = "Let's go";
    private SharedPreferences sharedPreferences;

    private static RemoteConfigSDK instance;

    private RemoteConfigSDK(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static RemoteConfigSDK getInstance(Context context){
        if (instance == null){
            instance = new RemoteConfigSDK(context);
        }
        return instance;
    }

    public void fetchRemoteConfig() {
        new FetchRemoteConfigTask().execute("http://192.168.2.162:3000/api/getButtonText");
    }

    private class FetchRemoteConfigTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String buttonText = jsonObject.getString("buttonText");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(BUTTON_TEXT_KEY, buttonText);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getButtonText() {
        return sharedPreferences.getString(BUTTON_TEXT_KEY, DEFAULT_BUTTON_TEXT);
    }
}
