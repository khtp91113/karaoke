package com.example.karaoke;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.karaoke.soundtouch.SoundTouch;

public class MusicActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;
    private MediaPlayer mediaPlayerOrigin = null;
    private boolean removeVocalState = true;

    private MediaPlayer mediaPlayerRight = null;
    private float pitch;
    private int pitchState;
    private final float pitch_interval = 1.05946f;
    protected static Handler handler;

    private float musicVolume = 0.5f;
    private float vocalVolume = 0.5f;

    private String outputRecordPath;
    private String outputMusicPath;
    private String outputTestPath;
    private String outputLyricPath;
    private String outputOriginPath;
    private String outputTmpPath;

    private String username = "ftpuser";
    private String password = "12345678";
    private String server = "140.116.245.248";
    private int port = 21;

    private String musicName = "";
    private String artistName = "";
    private int UID;

    private ProgressBar timeBar;
    private AlertDialog dialog = null;
    private ProgressDialog progressDialog = null;
    private boolean restartFlag = false;
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
        setContentView(R.layout.activity_music);
        // ask for permissions
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSION);

        // set seekbar listener
        SeekBar seekbarMusic = findViewById(R.id.seekBar_music);
        SeekBar seekbarVocal = findViewById(R.id.seekBar_vocal);
        seekbarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView musicValue = findViewById(R.id.music_value);
                musicValue.setText(String.valueOf(progress) + "%");
                musicVolume = progress / 100.0f;
                if (mediaPlayer != null){
                    mediaPlayer.setVolume(musicVolume, 0f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekbarVocal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView vocalValue = findViewById(R.id.vocal_value);
                vocalValue.setText(String.valueOf(progress) + "%");
                vocalVolume = progress / 100.0f;
                if (mediaPlayerRight != null){
                    mediaPlayerRight.setVolume(0f, vocalVolume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // hide record related buttons
        findViewById(R.id.listen).setVisibility(View.INVISIBLE);
        findViewById(R.id.text_music).setVisibility(View.INVISIBLE);
        findViewById(R.id.text_vocal).setVisibility(View.INVISIBLE);
        findViewById(R.id.seekBar_music).setVisibility(View.INVISIBLE);
        findViewById(R.id.seekBar_vocal).setVisibility(View.INVISIBLE);
        findViewById(R.id.music_value).setVisibility(View.INVISIBLE);
        findViewById(R.id.vocal_value).setVisibility(View.INVISIBLE);
        findViewById(R.id.adjust).setVisibility(View.INVISIBLE);
        findViewById(R.id.save).setVisibility(View.INVISIBLE);
        findViewById(R.id.upload).setVisibility(View.INVISIBLE);

        // set button long press listener
        setLongPress();

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                // show listen button, hide key increase & decrease button
                if (message.what == R.integer.SHOW_LISTEN_BUTTON){
                    if (progressDialog != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                    findViewById(R.id.listen).setVisibility(View.VISIBLE);
                    findViewById(R.id.adjust).setVisibility(View.VISIBLE);
                    findViewById(R.id.save).setVisibility(View.VISIBLE);
                    findViewById(R.id.key_decrease).setVisibility(View.INVISIBLE);
                    findViewById(R.id.key_increase).setVisibility(View.INVISIBLE);
                    findViewById(R.id.switchVersion).setVisibility(View.INVISIBLE);
                    findViewById(R.id.stop).setVisibility(View.INVISIBLE);
                    findViewById(R.id.restart).setEnabled(true);
                }
                else if(message.what == R.integer.CANT_CONNECT_HTTP_SERVER){
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
                                Toast.makeText(MusicActivity.this, "Download done", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    mediaPlayerSetup();
                }
                else if(message.what == R.integer.Upload_Done){
                    if (progressDialog != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                findViewById(R.id.upload).setEnabled(true);
                                Toast.makeText(MusicActivity.this, "Upload done", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    findViewById(R.id.restart).setEnabled(true);
                }
                else if(message.what == R.integer.Record_Done){
                    findViewById(R.id.restart).setEnabled(false);
                    if (restartFlag == true){
                        restartFlag = false;
                        // restart
                        handler.sendEmptyMessage(R.integer.Download_Done);
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog = ProgressDialog.show(MusicActivity.this, "Please wait", "Dealing record...", true, true);
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dealRecord();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

            }
        };

        Intent intent = getIntent();
        musicName = intent.getStringExtra("MusicName");
        artistName = intent.getStringExtra("ArtistName");
        //UID = intent.getIntExtra("UID", 21);
        UID = 21;

        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        outputMusicPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  + "_music.wav";
        outputRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_record.wav";
        outputTestPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_test.wav";
        outputLyricPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_lyric.lrc";
        outputOriginPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_origin.wav";
        outputTmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Karaoke/" + artistName + "-" + musicName  +  "_tmp.wav";

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        findViewById(R.id.lrcView).setVisibility(View.INVISIBLE);
        findViewById(R.id.restart).setEnabled(false);
        timeBar = findViewById(R.id.timeBar);
        // check internet status and hint
        checkInternet();
    }

    public void setLongPress(){
        int[] buttonList = {R.id.switchVersion, R.id.key_decrease, R.id.key_increase, R.id.listen, R.id.adjust, R.id.save, R.id.stop, R.id.restart, R.id.upload};
        for (int i = 0; i < buttonList.length; i++){
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

    public void popUpDetail(View v){
        View layout = LayoutInflater.from(this).inflate(R.layout.popup_content, null);
        popup = new PopupWindow(this);
        popup.setContentView(layout);
        int buttonClickID = v.getId();
        if (buttonClickID == R.id.switchVersion){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.switchVersion_detail);
        }
        else if (buttonClickID == R.id.key_decrease){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.key_decrease_detail);
        }
        else if (buttonClickID == R.id.key_increase){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.key_increase_detail);
        }
        else if (buttonClickID == R.id.listen){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.listen_detail);
        }
        else if (buttonClickID == R.id.adjust){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.adjust_detail);
        }
        else if (buttonClickID == R.id.save){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.save_detail);
        }
        else if (buttonClickID == R.id.stop){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.stop_detail);
        }
        else if (buttonClickID == R.id.restart){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.restart_detail);
        }
        else if (buttonClickID == R.id.upload){
            ((TextView)(layout.findViewById(R.id.detail))).setText(R.string.upload_detail);
        }

        // Set content width and height
        layout.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(layout.getMeasuredHeight());
        popup.setWidth(layout.getMeasuredWidth());

        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(v, 0, -(layout.getMeasuredHeight() + v.getHeight() + 8));
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
                    progressDialog = ProgressDialog.show(MusicActivity.this, "Please wait", "Downloading...", true, true);
                    new DownloadMusicTask().execute("http://140.116.245.248:5000", musicName, artistName, server, String.valueOf(port), username, password, outputMusicPath, outputOriginPath, outputLyricPath);
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
            progressDialog = ProgressDialog.show(MusicActivity.this, "Please wait", "Downloading...", true, true);
            new DownloadMusicTask().execute("http://140.116.245.248:5000", musicName, artistName, server, String.valueOf(port), username, password, outputMusicPath, outputOriginPath, outputLyricPath);
        }
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
        ((TextView)findViewById(R.id.endTime)).setText(minStr + ":" + secStr);
        ((TextView)findViewById(R.id.currentTime)).setText("00:00");

        /** Lyric initialize*/
        mLrcView=(LyricView)findViewById(R.id.lrcView);
        mLrcView.setVisibility(View.VISIBLE);
        String lrc = getFromAssets();

        LyricBuilder builder = new LyricBuilder();
        List<LyricRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);
    }

    public void mediaPlayerSetup(){
        // reset song version button text for restart
        ((ImageButton)(findViewById(R.id.switchVersion))).setImageResource(R.mipmap.origin);
        removeVocalState = true;
        // open music file and play
        File file = new File(outputMusicPath);
        File originFile = new File(outputOriginPath);
        Uri songUri = Uri.fromFile(file);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);

        Uri originUri = Uri.fromFile(originFile);
        mediaPlayerOrigin = MediaPlayer.create(getApplicationContext(), originUri);
        mediaPlayerOrigin.setVolume(0.0f, 0.0f);
        pitch = 1;
        pitchState = 0; // increase or decrease up to 5 levels

        initLyric();
        findViewById(R.id.restart).setEnabled(true);
        // use thread to record
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    record();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void keyIncrease(View view){
        Log.i("test", "increase press");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = new PlaybackParams();
            if (pitchState == 5){
                Toast.makeText(this, "Highest", Toast.LENGTH_SHORT).show();
                return;
            }
            pitch *= pitch_interval;
            pitchState += 1;
            params.setPitch(pitch);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayerOrigin.setPlaybackParams(params);
        }
    }

    public void keyDecrease(View view){
        Log.i("test", "decrease press");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = new PlaybackParams();
            if (pitchState == -5){
                Toast.makeText(this, "Lowest", Toast.LENGTH_SHORT).show();
                return;
            }
            pitch /= pitch_interval;
            pitchState -= 1;
            params.setPitch(pitch);
            mediaPlayer.setPlaybackParams(params);
            mediaPlayerOrigin.setPlaybackParams(params);
        }
    }

    // decode mp3 to PCM, then convert PCM to wav
    /*private void decode(String outputPath) throws IOException {
        //final AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.onion_mayday_cut_wav);
        MediaExtractor extractor = null;
        MediaFormat mediaFormat = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            extractor = new MediaExtractor();
            // set source music file
            extractor.setDataSource(outputMusicPath);
            //extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());

            // find audio track
            for (int i = 0; i < extractor.getTrackCount(); i++){
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")){
                    extractor.selectTrack(i);
                    mediaFormat = format;
                    break;
                }
            }
            // can't find audio track, return
            if (mediaFormat == null){
                extractor.release();
                Toast.makeText(this, "Can't find audio info", Toast.LENGTH_SHORT).show();
                return;
            }

            FileOutputStream output = new FileOutputStream(outputPath);
            String mediaMime = mediaFormat.getString(MediaFormat.KEY_MIME);
            MediaCodec codec = MediaCodec.createDecoderByType(mediaMime);
            codec.configure(mediaFormat, null, null, 0);
            codec.start();

            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();
            final long timeout = 5000;
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean inputEOF = false;

            // save music's final data after remove vocal
            List<Byte> outputList = new ArrayList<Byte>();

            while (true){
                // if not end of song
                if (!inputEOF){
                    int inputIndex = codec.dequeueInputBuffer(timeout); // return available buffer index, if no available buffer or timeout return -1
                    if (inputIndex >= 0){
                        ByteBuffer buf = inputBuffers[inputIndex];
                        // use available buffer to decode
                        int sampleSize = extractor.readSampleData(buf, 0);
                        if (sampleSize < 0){
                            // end of song
                            inputEOF = true;
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }
                        else{
                            long time = extractor.getSampleTime();
                            codec.queueInputBuffer(inputIndex, 0, sampleSize, time, 0);
                            extractor.advance(); // advance to next sample data
                        }
                    }
                }
                int outputIndex = codec.dequeueOutputBuffer(info, timeout); // get available output buffer
                if (outputIndex >= 0){
                    // ignore codec config buffers
                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
                        codec.releaseOutputBuffer(outputIndex, false);
                        continue;
                    }
                    if (info.size != 0){
                        ByteBuffer buf = outputBuffers[outputIndex];
                        buf.position(info.offset);
                        buf.limit(info.offset + info.size);
                        byte[] data = new byte[info.size];
                        // get decode data
                        buf.get(data);
                        // remove vocal: invert right channel, then add to left channel
                        // format: left, left, right, right
                        for (int i = 0; i < info.size; i += 4) {
                            byte combineLeft = (byte)(data[i] + data[i+2] * -1);
                            byte combineRight = (byte)(data[i+1] + data[i+3] * -1);
                            outputList.add(combineLeft);
                            outputList.add(combineRight);
                        }
                    }
                    codec.releaseOutputBuffer(outputIndex, false);

                    // end of output file, break
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                        break;
                    }
                }
                else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                    outputBuffers = codec.getOutputBuffers();
                }
            }

            // write wav header
            writeWavHeader(output, outputList);

            // convert Byte List to byte array
            Byte[] soundBytes = outputList.toArray(new Byte[outputList.size()]);
            byte[] outputSound = new byte[soundBytes.length];
            int j = 0;
            for(Byte b: soundBytes)
                outputSound[j++] = b.byteValue();
            // write wav data
            output.write(outputSound);

            output.close();
            codec.stop();
            codec.release();
            extractor.release();
        }
    }*/

    private void writeWavHeader(FileOutputStream output, int length) throws IOException {
        //write wav header
        short channel = 1;
        int sampleRate = 44100;
        short bitDepth = 16;

        byte[] chunkSize = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(36 + length / 2 * channel * bitDepth / 8)
                .array();

        byte[] headers = ByteBuffer.allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channel)
                .putInt(sampleRate)
                .putInt(sampleRate * channel * bitDepth / 8)
                .putShort((short) (channel * bitDepth / 8))
                .putShort(bitDepth)
                .array();

        byte[] subChunk2Size = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(length / 2 * channel * bitDepth / 8)
                .array();

        output.write(new byte[]{
                'R', 'I', 'F', 'F' //chunk ID
        });
        output.write(chunkSize);
        output.write(new byte[]{
                'W', 'A', 'V', 'E', //format
                'f', 'm', 't', ' ', //subchunk1 ID
                16,   0,   0,   0, //subchunk1 size
                1,   0 //audio format
        });
        output.write(headers);
        output.write(new byte[]{
                'd', 'a', 't', 'a' //subchunk2 ID
        });
        output.write(subChunk2Size);
    }

    public void stopRecord(View view){
        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
        if (mediaPlayerOrigin != null && mediaPlayerOrigin.isPlaying())
            mediaPlayerOrigin.stop();
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

    private void record() throws IOException {
        FileOutputStream tmpOutput = new FileOutputStream(outputTmpPath);
        int minSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);

        byte[] data = new byte[minSize];

        scrollLyric();
        mediaPlayer.start();
        audioRecord.startRecording();

        while (mediaPlayer.isPlaying()) {
            int size = audioRecord.read(data, 0, minSize);
            if (size != AudioRecord.ERROR_INVALID_OPERATION) {
                tmpOutput.write(data, 0, size);
            }
        }
        audioRecord.stop();
        audioRecord.release();
        mTimer.cancel();
        mTimer = null;

        tmpOutput.close();
        handler.sendEmptyMessage(R.integer.Record_Done);
    }

    public void dealRecord() throws IOException {
        InputStream tmpInput = new FileInputStream(outputTmpPath);
        byte[] data = new byte[tmpInput.available()];
        tmpInput.read(data);
        tmpInput.close();

        FileOutputStream output = new FileOutputStream(outputTmpPath);
        data = adjustRecordLength(data);

        writeWavHeader(output, data.length);

        output.write(data);
        output.close();

        modifyPitch();

        // show listen button
        handler.sendEmptyMessage(R.integer.SHOW_LISTEN_BUTTON);
    }

    public byte[] adjustRecordLength(byte[] data) throws IOException {
        // read music size
        InputStream musicStream = new FileInputStream(outputMusicPath);
        byte[] music = new byte[musicStream.available()];
        musicStream.read(music);
        musicStream.close();

        int musicDataSize = music.length - 44;
        int recordDataSize = data.length;
        if (recordDataSize > musicDataSize){
            return Arrays.copyOfRange(data, recordDataSize-musicDataSize, recordDataSize);
        }
        return data;
    }

    // modify music pitch for mix record
    public void modifyPitch() {
        SoundTouch st = new SoundTouch();
        st.setPitchSemiTones(pitchState);
        st.processFile(outputMusicPath, outputTestPath);
    }

    // listen to record mix with song
    public void listen_record(View view) {
        if (mediaPlayer == null || mediaPlayerRight == null || (mediaPlayer.isPlaying() == false && mediaPlayerRight.isPlaying() == false)){
            // open music & record file and play
            File fileMusic = new File(outputTestPath);
            File fileRecord = new File(outputTmpPath);
            Uri musicUri = Uri.fromFile(fileMusic);
            Uri recordUri = Uri.fromFile(fileRecord);
            mediaPlayer = MediaPlayer.create(getApplicationContext(), musicUri);
            mediaPlayerRight = MediaPlayer.create(getApplicationContext(), recordUri);

            mediaPlayerRight.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                        mTimer.cancel();
                }
            });

            /////// for test!!!!!! ////////////
            //mediaPlayerRight = MediaPlayer.create(getApplicationContext(), R.raw.onion_mayday_cut_wav);

            // play music in left channel, vocal in right channel
            // get volume, default 50% vocal 50% music
            mediaPlayer.setVolume(musicVolume, 0f);
            mediaPlayerRight.setVolume(0f, vocalVolume);
            initLyric();
            mTimer = null;
            scrollLyric();
            mediaPlayerRight.start();
            mediaPlayer.start();
        }
    }

    public void show_adjust(View view){
        int state = findViewById(R.id.text_music).getVisibility();
        // hide seekbar & text
        if (state == View.VISIBLE){
            findViewById(R.id.text_vocal).setVisibility(View.INVISIBLE);
            findViewById(R.id.text_music).setVisibility(View.INVISIBLE);
            findViewById(R.id.seekBar_vocal).setVisibility(View.INVISIBLE);
            findViewById(R.id.seekBar_music).setVisibility(View.INVISIBLE);
            findViewById(R.id.music_value).setVisibility(View.INVISIBLE);
            findViewById(R.id.vocal_value).setVisibility(View.INVISIBLE);
        }
        // show seekbar & text
        else{
            findViewById(R.id.text_vocal).setVisibility(View.VISIBLE);
            findViewById(R.id.text_music).setVisibility(View.VISIBLE);
            findViewById(R.id.seekBar_vocal).setVisibility(View.VISIBLE);
            findViewById(R.id.seekBar_music).setVisibility(View.VISIBLE);
            findViewById(R.id.music_value).setVisibility(View.VISIBLE);
            findViewById(R.id.vocal_value).setVisibility(View.VISIBLE);
        }
    }

    public void save(View view) throws IOException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.restart).setEnabled(false);
            // stop mediaplayer
            if (mediaPlayerRight != null && mediaPlayerRight.isPlaying()) {
                mediaPlayerRight.stop();
                mediaPlayerRight = null;
            }
            if (mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer = null;
            }
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            // read file
            InputStream musicStream = new FileInputStream(outputTestPath);
            InputStream vocalStream = new FileInputStream(outputTmpPath);

            ////// For test //////
            //InputStream vocalStream = getResources().openRawResource(R.raw.onion_mayday_cut_wav);

            byte[] music = new byte[musicStream.available()];
            musicStream.read(music);
            byte[] vocal = new byte[vocalStream.available()];
            vocalStream.read(vocal);

            // mix vocal & music, ignore 44 bytes header
            for (int i = 44; i < vocal.length; i += 2){
                // convert byte to short (16 bits)
                float sampleMusic = (music[i] & 0xFF | music[i+1] << 8) / 32768.0f;
                float sampleVocal = (vocal[i] & 0xFF | vocal[i+1] << 8) / 32768.0f;

                // adjust volume & mix
                sampleMusic *= musicVolume;
                sampleVocal *= vocalVolume;
                float mix = (sampleMusic + sampleVocal);
                // avoid too many noise
                if (mix > 1.0f)
                    mix = 1.0f;
                else if (mix < -1.0f)
                    mix = -1.0f;
                // save byte
                short output = (short)(mix * 32768.0f);
                vocal[i] = (byte)(output & 0xFF);
                vocal[i+1] = (byte)(output >> 8);
            }
            musicStream.close();
            vocalStream.close();

            // save
            FileOutputStream output = new FileOutputStream(outputRecordPath);
            // write wav header
            writeWavHeader(output, vocal.length);

            // write wav data
            output.write(vocal, 44, vocal.length-44);

            output.close();
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
            findViewById(R.id.upload).setVisibility(View.VISIBLE);
            findViewById(R.id.restart).setEnabled(true);
        }
    }

    public void upload(View view) {
        findViewById(R.id.upload).setEnabled(false);
        findViewById(R.id.restart).setEnabled(false);
        progressDialog = ProgressDialog.show(this, "Please wait", "Uploading...", true, true);
        new UploadRecordTask().execute("http://140.116.245.248:5000", musicName, artistName, server, String.valueOf(port), username, password, String.valueOf(UID), outputRecordPath);
    }

    // switch origin song or remove vocal version
    public void switchSong(View view){
        if (removeVocalState == true){
            removeVocalState = false;
            mediaPlayer.setVolume(0.0f, 0.0f);
            mediaPlayerOrigin.setVolume(1.0f, 1.0f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayerOrigin.seekTo(mediaPlayer.getCurrentPosition(), MediaPlayer.SEEK_CLOSEST);
            }
            mediaPlayerOrigin.start();
            ((ImageButton)(findViewById(R.id.switchVersion))).setImageResource(R.mipmap.remove_vocal);
        }
        else{
            removeVocalState = true;
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayerOrigin.setVolume(0.0f, 0.0f);
            mediaPlayerOrigin.pause();
            ((ImageButton)(findViewById(R.id.switchVersion))).setImageResource(R.mipmap.origin);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        File test = new File(outputTestPath);
        File tmp = new File(outputTmpPath);
        File music = new File(outputMusicPath);
        File origin = new File(outputOriginPath);
        if (tmp.exists() == true){
            tmp.delete();
        }
        if (test.exists() == true){
            test.delete();
        }
        if (origin.exists() == true){
            origin.delete();
        }
        if (music.exists() == true){
            music.delete();
        }
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

    /**定時task**/
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            final long timePassed = mediaPlayer.getCurrentPosition();
            MusicActivity.this.runOnUiThread(new Runnable() {
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
            MusicActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    int min = timePassed / 60000;
                    String minStr = String.valueOf(min);
                    if (min < 10)
                        minStr = "0" + minStr;
                    int sec = (timePassed / 1000) % 60;
                    String secStr = String.valueOf(sec);
                    if (sec < 10)
                        secStr = "0" + secStr;
                    ((TextView)findViewById(R.id.currentTime)).setText(minStr + ":" + secStr);
                    ((ProgressBar)findViewById(R.id.timeBar)).setProgress((int)(timePassed * 100.0f / totalTime));
                }
            });
        }
    }

    public void restart(View view){
        restartFlag = true;
        // restart if in record state
        if (findViewById(R.id.listen).getVisibility() == View.INVISIBLE) {
            stopRecord(view);
        }
        // restart if in listen state
        else {
            // adjust UI
            // hide record related buttons
            findViewById(R.id.listen).setVisibility(View.INVISIBLE);
            findViewById(R.id.text_music).setVisibility(View.INVISIBLE);
            findViewById(R.id.text_vocal).setVisibility(View.INVISIBLE);
            findViewById(R.id.seekBar_music).setVisibility(View.INVISIBLE);
            findViewById(R.id.seekBar_vocal).setVisibility(View.INVISIBLE);
            findViewById(R.id.music_value).setVisibility(View.INVISIBLE);
            findViewById(R.id.vocal_value).setVisibility(View.INVISIBLE);
            findViewById(R.id.adjust).setVisibility(View.INVISIBLE);
            findViewById(R.id.save).setVisibility(View.INVISIBLE);
            findViewById(R.id.upload).setVisibility(View.INVISIBLE);
            findViewById(R.id.switchVersion).setVisibility(View.VISIBLE);
            findViewById(R.id.key_decrease).setVisibility(View.VISIBLE);
            findViewById(R.id.key_increase).setVisibility(View.VISIBLE);
            findViewById(R.id.stop).setVisibility(View.VISIBLE);
            try {
                save(view);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessage(R.integer.Record_Done);
        }
    }
}
