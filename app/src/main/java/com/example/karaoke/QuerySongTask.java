package com.example.karaoke;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QuerySongTask extends AsyncTask<String, Void, String[]> {
    @Override
    protected String[] doInBackground(String... strings) {
        try {
            String param = "MusicName=" + strings[1];
            URL url = new URL(strings[0] + "/QuerySong");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty( "charset", "utf-8");
            connection.connect();

            OutputStream out = connection.getOutputStream();
            out.write(param.getBytes("utf-8"));
            out.flush();
            out.close();
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
                JSONArray arr = new JSONArray(result.toString());
                JSONObject json = arr.getJSONObject(0);
                String[] paths = new String[3];
                paths[0] = json.getString("Path");
                paths[1] = json.getString("Path2");
                paths[2] = json.getString("Path3");
                return paths;
            }
        }
        catch (IOException e){
            Log.i("test", e.getMessage());
        }
        catch (JSONException e){
            Log.i("test", e.getMessage());
        }
        return null;
    }
}
