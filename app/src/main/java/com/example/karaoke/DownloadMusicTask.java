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

public class DownloadMusicTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        String httpServer = strings[0];
        String musicName = strings[1];
        String ftpServer = strings[2];
        String port = strings[3];
        String username = strings[4];
        String password = strings[5];
        String outputMusicPath = strings[6];
        String outputOriginPath = strings[7];
        String outputLyricPath = strings[8];

        Handler handler = MusicActivity.handler;
        String[] paths = new String[3];
        try {
            String param = "MusicName=" + strings[1];
            URL url = new URL(httpServer + "/QuerySong");
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
                paths[0] = json.getString("Path");
                paths[1] = json.getString("Path2");
                paths[2] = json.getString("Path3");
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
                // switch to music folder
                ftp.changeWorkingDirectory(paths[0]);
                FileOutputStream outputMusic = new FileOutputStream(outputMusicPath);
                String songPath = new String((musicName + ".wav").getBytes("utf-8"), "iso-8859-1");
                ftp.retrieveFile(songPath, outputMusic);
                outputMusic.close();
                // switch to removeVocal folder
                ftp.changeWorkingDirectory(paths[1]);
                FileOutputStream outputOrigin = new FileOutputStream(outputOriginPath);
                String originPath = new String((musicName + ".wav").getBytes("utf-8"), "iso-8859-1");
                ftp.retrieveFile(originPath, outputOrigin);
                outputMusic.close();
                // switch to lyric folder
                ftp.changeToParentDirectory();
                ftp.changeWorkingDirectory(paths[2]);
                FileOutputStream outputLyric = new FileOutputStream(outputLyricPath);
                String lyricPath = new String((musicName + ".txt").getBytes("utf-8"), "iso-8859-1");
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
