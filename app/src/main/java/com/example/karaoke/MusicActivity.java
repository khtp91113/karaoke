package com.example.karaoke;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = null;
    private float pitch;
    private int pitchState;
    private final float pitch_interval = 1.05946f;

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

    }

    public void loadMusic(View view) throws IOException {
        // if music is playing, avoid click button again
        if (mediaPlayer != null && mediaPlayer.isPlaying() == false){
            return;
        }
        // decode mp3 file, remove vocal, save as wav file
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.wav";
        decode(outputPath); // it takes about 30 seconds

        // open music file and play
        File file = new File(outputPath);
        Uri songUri = Uri.fromFile(file);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri);
        pitch = 1;
        pitchState = 0; // increase or decrease up to 5 levels
        mediaPlayer.start();
    }

    public void keyIncrease(View view){
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PlaybackParams params = new PlaybackParams();
            if (pitch == -5){
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
        final AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.onion_mayday);
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

            //write wav header
            short channel = 1;
            int sampleRate = 44100;
            short bitDepth = 16;

            byte[] chunkSize = ByteBuffer.allocate(4)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .putInt(36 + outputList.size() * channel * bitDepth / 8)
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
                                    .putInt(outputList.size() * channel * bitDepth / 8)
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

}
