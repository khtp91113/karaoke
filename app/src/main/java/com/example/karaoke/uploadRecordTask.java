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

public class uploadRecordTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        try {
            String param = "PersonalMusicName=" + strings[1] + "&UID=" + strings[2];
            URL url = new URL(strings[0] + "/UploadPersonalSong");
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
                String line = reader.readLine();
                reader.close();
                return line;
            }
        }
        catch (IOException e){
            Log.i("test", e.getMessage());
        }
        return null;
    }
}
