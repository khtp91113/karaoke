package com.example.karaoke;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadRecordTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String httpServer = strings[0];
        String musicName = strings[1];
        String artistName = strings[2];
        String ftpServer = strings[3];
        String port = strings[4];
        String username = strings[5];
        String password = strings[6];
        String UID = strings[7];
        String outputRecordPath = strings[8];

        String path;
        Handler handler = MusicActivity.handler;
        try {
            String param = "PersonalMusicName=" + musicName + "&UID=" + UID;
            URL url = new URL(httpServer + "/UploadPersonalSong");
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
                path = line;
            }
            else{
                handler.sendEmptyMessage(R.integer.CANT_CONNECT_HTTP_SERVER);
                return null;
            }
        }
        catch (IOException e){
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
                // switch to Record folder
                if (ftp.changeWorkingDirectory(path) == false) {
                    ftp.makeDirectory(path);
                    ftp.changeWorkingDirectory(path);
                }
                FileInputStream inputRecord = new FileInputStream(outputRecordPath);
                String songPath = new String((artistName + "-" + musicName + ".wav").getBytes("utf-8"), "iso-8859-1");
                ftp.storeFile(songPath, inputRecord);
                inputRecord.close();
                ftp.logout();
                ftp.disconnect();
                handler.sendEmptyMessage(R.integer.Upload_Done);
                return null;
            }
            else {
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
