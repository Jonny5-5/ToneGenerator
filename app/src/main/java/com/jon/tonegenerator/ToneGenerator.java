package com.jon.tonegenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class ToneGenerator {

    private int sampleRate = 16000;
    private int numSamples = 16000;
    private double sample[] = new double[numSamples];
    private final byte soundBuffer[] = new byte[2 * numSamples];
    private double soundFrequency = 0;
    private boolean isPlaying = false, isWriting = false;
    private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
            sampleRate, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, sampleRate,
            AudioTrack.MODE_STREAM);

    private double phaseShift = 0;
    private void generateTone(){
        int i;
        for (i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2.0 * Math.PI * i / ( (double)sampleRate / soundFrequency) + phaseShift);
        }
        phaseShift = 2.0 * Math.PI * (i) / ( (double)sampleRate / soundFrequency) + phaseShift;
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            soundBuffer[idx++] = (byte) (val & 0x00ff);
            soundBuffer[idx++] = (byte) ((val & 0xff00) >>> 8);
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
                    audioTrack.write(soundBuffer, 0, soundBuffer.length);
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
