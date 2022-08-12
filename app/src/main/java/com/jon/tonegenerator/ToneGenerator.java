package com.jon.tonegenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

// TODO: Make a circular array for storing / calculating the data or...
// TODO: I can just keep track of what index I'm on and then calculate each value on-the-fly
// TODO: When it reaches MAX_INDEX (some multiple of 2 pi) then I set it equal to 0 again



public class ToneGenerator {

    final String TAG = "TONE GENERATOR";
    private int SAMPLE_RATE = 16000;
    private int NUM_SAMPLES = 16000;
    private double SAMPLES[] = new double[NUM_SAMPLES];
    private final byte SOUND_BUFFER[] = new byte[2 * NUM_SAMPLES];
    private double soundFrequency = 0;
    private boolean isPlaying = false, isWriting = false;
    private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, SAMPLE_RATE,
            AudioTrack.MODE_STREAM);

    private void generateTone(){

        if (soundFrequency == 0){
            Log.d(TAG, "The soundFrequency is 0. Set the freq");
            return;
        }
        for (int i = 0; i < NUM_SAMPLES; ++i) {
            SAMPLES[i] = Math.sin(2.0 * Math.PI * i / ((double) SAMPLE_RATE / soundFrequency));
        }
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        int idx = 0;
        for (final double dVal : SAMPLES) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            SOUND_BUFFER[idx++] = (byte) (val & 0x00ff);
            SOUND_BUFFER[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    public void start(){
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED){
            audioTrack.flush();
        }
        isPlaying = true;
        final Thread playerThread = new Thread(new Runnable() {
            public void run() {
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
                    audioTrack.play();
                generateTone();
                while (isPlaying && !isWriting){
                    isWriting = true;
                    // TODO: Use the NON_BLOCKING method in the future
                    audioTrack.write(SOUND_BUFFER, 0, SOUND_BUFFER.length);
                    isWriting = false;
                }
                audioTrack.pause();
            }
        });
        playerThread.start();
    }

    public void stop(){ isPlaying = false; }
    public double getSoundFrequency() {
        return soundFrequency;
    }
    public void setSoundFrequency(double soundFrequency) {
        //round the frequency to avoid clicks and beeps in the audioTrack
        soundFrequency = Math.round( soundFrequency );
        this.soundFrequency = soundFrequency;
    }
    public boolean isPlaying() {
        return isPlaying;
    }
    public void refresh(){
        generateTone();
    }

}
