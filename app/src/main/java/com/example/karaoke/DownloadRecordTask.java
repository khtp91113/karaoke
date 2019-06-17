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

public class DownloadRecordTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        String httpServer = strings[0];
        String musicName = strings[1];
        String artistName = strings[2];
        String ftpServer = strings[3];
        String port = strings[4];
        String username = strings[5];
        String password = strings[6];
        String outputRecordPath = strings[7];
        String outputLyricPath = strings[8];
        int UID = Integer.parseInt(strings[9]);

        Handler handler = ListenRecordActivity.handler;
        String[] paths = new String[2];
        try {
            String param = "PersonalMusicName=" + artistName + "-" + musicName + "&UID=" + UID;
            URL url = new URL(httpServer + "/QueryPersonalSong");
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
                paths[0] = json.getString("RecordPath");
                paths[1] = json.getString("LyricPath");
            }
        }
        catch (IOException e){
            Log.i("test", e.getMessage());
            handler.sendEmptyMessage(R.integer.CANT_CONNECT_HTTP_SERVER);
            return null;
        }
        catch (JSONException e){
            Log.i("test", e.getMessage());
            handler.sendEmptyMessage(R.integer.CANT_CONNECT_HTTP_SERVER);
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
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                // encrypt channel
                ftp.execPROT("P");
                ftp.enterLocalPassiveMode();
                // switch to Record Folder
                ftp.changeWorkingDirectory(paths[0]);
                FileOutputStream outputRecord = new FileOutputStream(outputRecordPath);
                Log.i("show output", outputRecordPath);
                String songPath = new String((artistName + "-" + musicName + ".wav").getBytes("utf-8"), "iso-8859-1");
                ftp.retrieveFile(songPath, outputRecord);
                outputRecord.close();
                // switch to lyric folder
                ftp.changeWorkingDirectory(paths[1]);
                FileOutputStream outputLyric = new FileOutputStream(outputLyricPath);
                String lyricPath = new String((artistName + "-" + musicName + ".lrc").getBytes("utf-8"), "iso-8859-1");
                ftp.retrieveFile(lyricPath, outputLyric);
                outputLyric.close();
                ftp.logout();
                ftp.disconnect();
                handler.sendEmptyMessage(R.integer.Download_Done);
                return null;
            }
            else{
                handler.sendEmptyMessage(R.integer.CANT_CONNECT_FTP_SERVER);
                return null;
            }
        }
        catch (IOException e){
            handler.sendEmptyMessage(R.integer.CANT_CONNECT_FTP_SERVER);
            return null;
        }
    }
}
