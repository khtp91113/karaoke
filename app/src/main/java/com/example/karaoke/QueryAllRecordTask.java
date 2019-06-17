package com.example.karaoke;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueryAllRecordTask extends AsyncTask<String, Void, RecordObject[]> {
    @Override
    protected RecordObject[] doInBackground(String... strings) {
        String httpServer = strings[0];

        String[] paths = new String[2];
        try {
            URL url = new URL(httpServer + "/QueryAllRecords");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.connect();

            // connect success
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer result = new StringBuffer();
                String line;
                // read response
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                // return format [ {...}, {...}, ...]
                Log.i("show", result.toString());
                JSONArray arr = new JSONArray(result.toString());
                Log.i("show", String.valueOf(arr.length()));
                RecordObject[] records = new RecordObject[arr.length()];
                for (int i = 0; i < arr.length(); i++){
                    JSONObject json = arr.getJSONObject(i);
                    records[i] = new RecordObject();
                    records[i].setName(json.getString("PersonalMusicName"));
                    records[i].setTimeStamp(json.getString("Timestamp"));
                    records[i].setUID(json.getInt("UID"));
                    records[i].setUserName(json.getString("userName"));
                }
                return records;
            }
        } catch (IOException e) {
            Log.i("test", e.getMessage());
            return null;
        } catch (JSONException e) {
            Log.i("test", e.getMessage());
            return null;
        }
        return null;
    }
}
