package com.tagmate;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class RemoteConfigSDK {
    private static final String TAG = "RemoteConfigSDK";
    private static final String DEFAULT_BUTTON_TEXT = "Let's go";

    private static RemoteConfigSDK instance;

    private String buttonText = DEFAULT_BUTTON_TEXT;

    private RemoteConfigSDK() {}

    public static RemoteConfigSDK getInstance() {
        if (instance == null) {
            instance = new RemoteConfigSDK();
        }
        return instance;
    }

    public void fetchRemoteConfig(OnRemoteConfigFetchedListener listener) {
        new FetchRemoteConfigTask(listener).execute("http://192.168.253.199:3000/api/getButtonText");
    }

    private class FetchRemoteConfigTask extends AsyncTask<String, Void, String> {
        private OnRemoteConfigFetchedListener listener;

        public FetchRemoteConfigTask(OnRemoteConfigFetchedListener listener) {
            this.listener = listener;
        }

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
                Log.e(TAG, "Error during API call", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                Log.d(TAG, "API Response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    buttonText = jsonObject.getString("buttonText");
                    Log.d(TAG, "Button Text Updated: " + buttonText);
                    if (listener != null) {
                        listener.onRemoteConfigFetched(buttonText);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON response", e);
                }
            } else {
                Log.e(TAG, "No response received from API");
                if (listener != null) {
                    listener.onRemoteConfigFetched(DEFAULT_BUTTON_TEXT);
                }
            }
        }
    }

    public String getButtonText() {
        return buttonText;
    }

    public interface OnRemoteConfigFetchedListener {
        void onRemoteConfigFetched(String buttonText);
    }
}
