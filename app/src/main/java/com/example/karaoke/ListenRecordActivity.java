package com.example.karaoke;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListenRecordActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;
    protected static Handler handler;

    private String outputRecordPath;
    private String outputLyricPath;

    private String username = "ftpuser";
    private String password = "12345678";
    private String server = "140.116.245.248";
    private int port = 21;

    private String musicName = "";
    private String artistName = "";
    private int UID;
    private String mode = "";

    private ProgressBar timeBar;
    private AlertDialog dialog = null;
    private ProgressDialog progressDialog = null;
    private PopupWindow popup;
    private boolean longPressFlag = false;

    String[] permissions = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.RECORD_AUDIO};
    private int ALL_PERMISSION = 101;

    /**Lyric params*/

    LyricView mLrcView;
    private int mPlayerTimerDuration = 100; //每100ms更新歌詞
    private Timer mTimer;
    private TimerTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_record);

        // ask for permissions
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSION);

        Intent intent = getIntent();
        musicName = intent.getStringExtra("MusicName");
        artistName = intent.getStringExtra("ArtistName");
        UID = intent.getIntExtra("UID", 21);
        mode = intent.getStringExtra("mode");

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        findViewById(R.id.lrcViewRecord).setVisibility(View.INVISIBLE);
        timeBar = findViewById(R.id.timeBarRecord);

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == R.integer.CANT_CONNECT_HTTP_SERVER){
                    server_disconnect_alert("http");
                }
                else if(message.what == R.integer.CANT_CONNECT_FTP_SERVER){
                    server_disconnect_alert("ftp");
                }
                else if(message.what == R.integer.Download_Done){
                    if (progressDialog != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(ListenRecordActivity.this, "Download done", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        };
        setLongPress();

        if (mode.equals("other")){
            outputRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_tmp_record.wav";
        }
        else{
            outputRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_record.wav";
        }
        outputLyricPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_lyric.lrc";
        File recordFile = new File(outputRecordPath);
        File lyricFile = new File(outputLyricPath);
        if (mode.equals("other"))
            checkInternet();
        else{
            if (recordFile.exists() == false || lyricFile.exists() == false)
                checkInternet();
        }

    }

    public void checkInternet(){
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        if (isConnected == false){
            // pop-up notice no network
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please check your network status!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI){
            // pop-up notice downloading
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure not using WiFi for downloading music?");
            builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressDialog = ProgressDialog.show(ListenRecordActivity.this, "Please wait", "Downloading...", true, true);
                    new DownloadRecordTask().execute("http://140.116.245.248:5000", musicName, artistName, server, String.valueOf(port), username, password, outputRecordPath, outputLyricPath, String.valueOf(UID));
                }
            });
            builder.setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            progressDialog = ProgressDialog.show(ListenRecordActivity.this, "Please wait", "Downloading...", true, true);
            new DownloadRecordTask().execute("http://140.116.245.248:5000", musicName, artistName, server, String.valueOf(port), username, password, outputRecordPath, outputLyricPath, String.valueOf(UID));
        }
    }

    public void server_disconnect_alert(String server){
        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Can't connect to " + server + " server");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /**讀取歌詞內容**/
    public String getFromAssets(){
        try {
            //InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) );
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(outputLyricPath));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void initLyric(){
        // init time
        timeBar.setProgress(0);
        int totalTime = mediaPlayer.getDuration();
        int min = totalTime / 60000;
        String minStr = String.valueOf(min);
        if (min < 10)
            minStr = "0" + minStr;
        int sec = (totalTime / 1000) % 60;
        String secStr = String.valueOf(sec);
        if (sec < 10)
            secStr = "0" + secStr;
        ((TextView)findViewById(R.id.endTimeRecord)).setText(minStr + ":" + secStr);
        ((TextView)findViewById(R.id.currentTimeRecord)).setText("00:00");

        /** Lyric initialize*/
        mLrcView=(LyricView)findViewById(R.id.lrcViewRecord);
        mLrcView.setVisibility(View.VISIBLE);
        String lrc = getFromAssets();

        LyricBuilder builder = new LyricBuilder();
        List<LyricRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
    }

    public void scrollLyric(){
        /**lyric scrolling*/
        if (mTimer == null) {
            mTimer = new Timer();
            mTask = new LrcTask();
            TimerTask timeTask = new TimeTask();
            mTimer.scheduleAtFixedRate(mTask, 0, mPlayerTimerDuration);
            mTimer.scheduleAtFixedRate(timeTask,0, 500);
        }
    }

    /**定時task**/
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            final long timePassed = mediaPlayer.getCurrentPosition();
            ListenRecordActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //歌詞向上滾動
                    mLrcView.seekLrcToTime(timePassed);
                }
            });
        }
    };

    // update current time
    class TimeTask extends TimerTask{
        @Override
        public void run() {
            final int timePassed = mediaPlayer.getCurrentPosition();
            final int totalTime = mediaPlayer.getDuration();
            ListenRecordActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    int min = timePassed / 60000;
                    String minStr = String.valueOf(min);
                    if (min < 10)
                        minStr = "0" + minStr;
                    int sec = (timePassed / 1000) % 60;
                    String secStr = String.valueOf(sec);
                    if (sec < 10)
                        secStr = "0" + secStr;
                    ((TextView)findViewById(R.id.currentTimeRecord)).setText(minStr + ":" + secStr);
                    ((ProgressBar)findViewById(R.id.timeBarRecord)).setProgress((int)(timePassed * 100.0f / totalTime));
                }
            });
        }
    }

    public void playRecordFromFile(View view){
        // open music file and play
        File file = new File(outputRecordPath);
        Uri songUri = Uri.fromFile(file);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);
        initLyric();
        scrollLyric();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mTimer.cancel();
                mTimer = null;
            }
        });
        mediaPlayer.start();
    }

    public void stop(View stop){
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.seekTo(mediaPlayer.getDuration());
    }

    // pop up detail
    public void popUpDetail(View v){
        View layout = LayoutInflater.from(this).inflate(R.layout.popup_content, null);
        popup = new PopupWindow(this);
        popup.setContentView(layout);
        int buttonClickID = v.getId();
        if (buttonClickID == R.id.listenRecord){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.listen_upload_record_detail);
        }
        else if (buttonClickID == R.id.stopPlayRecord){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.stop_play_record_detail);
        }

        // Set content width and height
        layout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(layout.getMeasuredHeight());
        popup.setWidth(layout.getMeasuredWidth());

        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(v, 0, -(layout.getMeasuredHeight() + v.getHeight() + 8));
    }

    public void setLongPress() {
        int[] buttonList = {R.id.listenRecord, R.id.stopPlayRecord};
        for (int i = 0; i < buttonList.length; i++) {
            findViewById(buttonList[i]).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longPressFlag = true;
                    popUpDetail(v);
                    return true;
                }
            });
            findViewById(buttonList[i]).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (longPressFlag) {
                            if (popup != null)
                                popup.dismiss();
                            longPressFlag = false;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mode.equals("other")){
            File record = new File(outputRecordPath);
            if (record.exists() == true){
                record.delete();
            }
        }
    }
}
