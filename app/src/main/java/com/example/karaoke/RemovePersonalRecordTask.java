package com.example.karaoke;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RemovePersonalRecordTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String httpServer = strings[0];
        String musicName = strings[1];
        String artistName = strings[2];
        String ftpServer = strings[3];
        String port = strings[4];
        String username = strings[5];
        String password = strings[6];
        int UID = Integer.parseInt(strings[7]);

        String path = "";
        try {
            String param = "PersonalMusicName=" + artistName + "-" + musicName + "&UID=" + UID;
            URL url = new URL(httpServer + "/RemovePersonalSong");
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
                String line;
                // read response
                while ((line = reader.readLine()) != null) {
                    path = line;
                }
                reader.close();
            }
        }
        catch (IOException e){
            return null;
        }
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            FTPSClient ftp = new FTPSClient();
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            ftp.setConnectTimeout(5000);
            ftp.connect(ftpServer, Integer.parseInt(port));

            int reply = ftp.getReplyCode();
            // if connect success
            if (FTPReply.isPositiveCompletion(reply)) {
                ftp.login(username, password);
                //ftp.setFileType(FTP.BINARY_FILE_TYPE);
                // encrypt channel
                //ftp.execPROT("P");
                //ftp.enterLocalPassiveMode();
                // switch to Record Folder
                //ftp.changeWorkingDirectory(path);
                path = new String((path + "/" + artistName + "-" + musicName + ".wav").getBytes("utf-8"), "iso-8859-1");
                boolean result = ftp.deleteFile(path);
                Log.i("result", String.valueOf(result));
                ftp.logout();
                ftp.disconnect();
                return null;
            }
            else{
                return null;
            }
        }
        catch (IOException e){
            return null;
        }
    }
}
