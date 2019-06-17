package com.example.karaoke;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MusicActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;
    private MediaPlayer mediaPlayerLeft = null;
    private MediaPlayer mediaPlayerRight = null;
    private float pitch;
    private int pitchState;
    private final float pitch_interval = 1.05946f;
    private int song_id = R.raw.onion_mayday;
    private Handler handler;
    private final int SHOW_LISTEN_BUTTON = 0;
    private float musicVolume = 0.5f;
    private float vocalVolume = 0.5f;
    private String outputRecordPath;
    private String outputMusicPath;

    /**Lyric params*/

    LyricView mLrcView;
    private int mPlayerTimerDuration = 100; //每100ms更新歌詞
    private Timer mTimer;
    private TimerTask mTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        // ask for external storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
        }

        // set seekbar listener
        SeekBar seekbarMusic = findViewById(R.id.seekBar_music);
        SeekBar seekbarVocal = findViewById(R.id.seekBar_vocal);
        seekbarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView musicValue = findViewById(R.id.music_value);
                musicValue.setText(String.valueOf(progress) + "%");
                musicVolume = progress / 100.0f;
                if (mediaPlayerLeft != null){
                    mediaPlayerLeft.setVolume(musicVolume, 0f);
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
        findViewById(R.id.share).setVisibility(View.INVISIBLE);
        findViewById(R.id.upload).setVisibility(View.INVISIBLE);

        handler = new Handler(){
            @Override
            public void handleMessage(Message message){
                // show listen button, hide key increase & decrease button
                if (message.what == SHOW_LISTEN_BUTTON){
                    findViewById(R.id.listen).setVisibility(View.VISIBLE);
                    findViewById(R.id.adjust).setVisibility(View.VISIBLE);
                    findViewById(R.id.save).setVisibility(View.VISIBLE);
                    findViewById(R.id.share).setVisibility(View.VISIBLE);
                    findViewById(R.id.upload).setVisibility(View.VISIBLE);
                    findViewById(R.id.flatButton).setVisibility(View.INVISIBLE);
                    findViewById(R.id.flatImage).setVisibility(View.INVISIBLE);
                    findViewById(R.id.sharpButton).setVisibility(View.INVISIBLE);
                    findViewById(R.id.sharpImage).setVisibility(View.INVISIBLE);
                }
            }
        };

        outputMusicPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/music.mp3";
        outputRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.wav";

        /** Lyric initialize*/
        mLrcView=(LyricView)findViewById(R.id.lrcView);
        String lrc = getFromAssets("FripSide-only my railgun.lrc");
        LyricBuilder builder = new LyricBuilder();
        List<LyricRow> rows = builder.getLrcRows(lrc);
        mLrcView.setLrc(rows);

    }

    public void loadMusic(View view) throws IOException {
        // if music is playing, avoid click button again
        if (mediaPlayer != null && mediaPlayer.isPlaying() == false){
            return;
        }
        // decode mp3 file, remove vocal, save as wav file
        File file = new File(outputMusicPath);
        // check if file exist
        if(file.exists() == false)
            decode(outputMusicPath); // it takes about 30 seconds
        // open music file and play
        Uri songUri = Uri.fromFile(file);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);
        pitch = 1;
        pitchState = 0; // increase or decrease up to 5 levels

        // use thread to record
        new Thread(new Runnable(){
            @Override
            public void run(){
                String outputRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/record.wav";
                try {
                    record(outputRecordPath);
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
        }
    }

    // decode mp3 to PCM, then convert PCM to wav
    private void decode(String outputPath) throws IOException {
        final AssetFileDescriptor fd = getResources().openRawResourceFd(song_id);
        MediaExtractor extractor = null;
        MediaFormat mediaFormat = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            extractor = new MediaExtractor();
            // set source music file
            extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());

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
    }

    private void writeWavHeader(FileOutputStream output, List<Byte> outputList) throws IOException {
        //write wav header
        short channel = 1;
        int sampleRate = 44100;
        short bitDepth = 16;

        byte[] chunkSize = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(36 + outputList.size() / 2 * channel * bitDepth / 8)
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
                .putInt(outputList.size() / 2 * channel * bitDepth / 8)
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

    private void record(String outputPath) throws IOException {
        int minSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,  AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);

        byte[] data = new byte[minSize];
        List<Byte> outputList = new ArrayList<Byte>();

        FileOutputStream output = new FileOutputStream(outputPath);
        audioRecord.startRecording();
        mediaPlayer.start();


        /**lyric scrolling*/

        if(mTimer == null){
            mTimer = new Timer();
            mTask = new LrcTask();
            mTimer.scheduleAtFixedRate(mTask, 0, mPlayerTimerDuration);
        }



        while(mediaPlayer.isPlaying()){
            int size = audioRecord.read(data, 0, minSize);
            if (size != AudioRecord.ERROR_INVALID_OPERATION){
                for (byte b: data)
                    outputList.add(b);
            }
        }
        audioRecord.stop();
        audioRecord.release();

        writeWavHeader(output, outputList);
        Byte[] soundBytes = outputList.toArray(new Byte[outputList.size()]);
        byte[] outputSound = new byte[soundBytes.length];
        int j = 0;
        for(Byte b: soundBytes)
            outputSound[j++] = b.byteValue();
        // write wav data
        output.write(outputSound);

        output.close();

        // show listen button
        Message msg = Message.obtain();
        msg.what = SHOW_LISTEN_BUTTON;
        handler.sendMessage(msg);
    }

    // listen to record mix with song
    public void listen_record(View view){
        if (mediaPlayerLeft == null && mediaPlayerRight == null){
            // open music & record file and play
            File fileMusic = new File(outputMusicPath);
            //File fileRecord = new File(outputRecordPath);
            Uri musicUri = Uri.fromFile(fileMusic);
            //Uri recordUri = Uri.fromFile(fileRecord);
            mediaPlayerLeft = MediaPlayer.create(getApplicationContext(), musicUri);
            //mediaPlayerRight = MediaPlayer.create(getApplicationContext(), recordUri);

            /////// for test!!!!!! ////////////
            mediaPlayerRight = MediaPlayer.create(getApplicationContext(), R.raw.onion_mayday);

            // play music in left channel, vocal in right channel
            // get volume, default 50% vocal 50% music
            mediaPlayerLeft.setVolume(musicVolume, 0f);
            mediaPlayerRight.setVolume(0f, vocalVolume);

            mediaPlayerLeft.start();
            mediaPlayerRight.start();
        }
        else if(mediaPlayerLeft.isPlaying() == false && mediaPlayerRight.isPlaying() == false){
            // play music in left channel, vocal in right channel
            // get volume, default 50% vocal 50% music
            mediaPlayerLeft.setVolume(musicVolume, 0f);
            mediaPlayerRight.setVolume(0f, vocalVolume);

            mediaPlayerLeft.start();
            mediaPlayerRight.start();
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
            // stop mediaplayer
            if (mediaPlayerRight != null && mediaPlayerRight.isPlaying()) {
                mediaPlayerRight.stop();
                mediaPlayerRight = null;
            }
            if (mediaPlayerLeft != null && mediaPlayerLeft.isPlaying()){
                mediaPlayerLeft.stop();
                mediaPlayerLeft = null;
            }
            // read file
            InputStream musicStream = new FileInputStream(outputMusicPath);
            //InputStream vocalStream = new FileInputStream(outputRecordPath);

            ////// For test //////
            InputStream vocalStream = getResources().openRawResource(R.raw.onion_mayday);

            byte[] music = new byte[musicStream.available()];
            musicStream.read(music);
            byte[] vocal = new byte[vocalStream.available()];
            vocalStream.read(vocal);

            List<Byte> outputList = new ArrayList<Byte>();
            // mix vocal & music, ignore 44 bytes header
            for (int i = 44; i < music.length; i += 2){
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
                outputList.add((byte)(output & 0xFF));
                outputList.add((byte)(output >> 8));
            }
            musicStream.close();
            vocalStream.close();

            // save
            FileOutputStream output = new FileOutputStream(outputRecordPath);
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
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void share(View view){
        // TODO
    }

    public void upload(View view){
        // TODO
    }


    /**讀取歌詞內容**/

    public String getFromAssets(String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) );
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
}
