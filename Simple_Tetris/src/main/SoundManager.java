package main;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static SoundManager instance;
    private boolean muted = false;
    private final ExecutorService soundPool;
    private final float SAMPLE_RATE = 22050f;

    private SoundManager() {
        // Single thread pool to avoid overwhelming audio drivers
        soundPool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "TetrisSoundThread");
            t.setDaemon(true);
            return t;
        });
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        this.muted = !this.muted;
    }

    public void playMove() {
        if (muted) return;
        soundPool.submit(() -> playTone(600, 20, 0.25f, WaveType.SQUARE));
    }

    public void playRotate() {
        if (muted) return;
        soundPool.submit(() -> playFrequencySlide(500, 800, 35, 0.3f, WaveType.SQUARE));
    }

    public void playHardDrop() {
        if (muted) return;
        soundPool.submit(() -> playFrequencySlide(300, 80, 55, 0.45f, WaveType.TRIANGLE));
    }

    public void playHold() {
        if (muted) return;
        soundPool.submit(() -> {
            playTone(700, 25, 0.25f, WaveType.SQUARE);
            try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            playTone(950, 35, 0.25f, WaveType.SQUARE);
        });
    }

    public void playLineClear() {
        if (muted) return;
        soundPool.submit(() -> {
            int[] notes = {523, 659, 784}; // C5, E5, G5
            for (int note : notes) {
                playTone(note, 45, 0.35f, WaveType.SQUARE);
                try { Thread.sleep(45); } catch (InterruptedException ignored) {}
            }
        });
    }

    public void playTetrisClear() {
        if (muted) return;
        soundPool.submit(() -> {
            int[] notes = {523, 659, 784, 1046}; // C5, E5, G5, C6
            for (int note : notes) {
                playTone(note, 60, 0.4f, WaveType.SQUARE);
                try { Thread.sleep(60); } catch (InterruptedException ignored) {}
            }
        });
    }

    public void playGameOver() {
        if (muted) return;
        soundPool.submit(() -> playFrequencySlide(450, 100, 400, 0.5f, WaveType.SAWTOOTH));
    }

    private enum WaveType { SINE, SQUARE, TRIANGLE, SAWTOOTH }

    private void playTone(double freq, int durationMs, float volume, WaveType type) {
        int samples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double time = i / (double) SAMPLE_RATE;
            double angle = 2.0 * Math.PI * freq * time;
            double sampleValue = 0;
            switch (type) {
                case SINE:
                    sampleValue = Math.sin(angle);
                    break;
                case SQUARE:
                    sampleValue = Math.sin(angle) >= 0 ? 1.0 : -1.0;
                    break;
                case TRIANGLE:
                    sampleValue = Math.asin(Math.sin(angle)) * (2.0 / Math.PI);
                    break;
                case SAWTOOTH:
                    sampleValue = 2.0 * (time * freq - Math.floor(time * freq + 0.5));
                    break;
            }
            // Linear decay envelope to prevent popping
            double envelope = 1.0 - ((double) i / samples);
            buffer[i] = (byte) (sampleValue * volume * envelope * 127);
        }
        playRawAudio(buffer);
    }

    private void playFrequencySlide(double startFreq, double endFreq, int durationMs, float volume, WaveType type) {
        int samples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        byte[] buffer = new byte[samples];
        for (int i = 0; i < samples; i++) {
            double progress = (double) i / samples;
            double currentFreq = startFreq + (endFreq - startFreq) * progress;
            double time = i / (double) SAMPLE_RATE;
            double angle = 2.0 * Math.PI * currentFreq * time;
            double sampleValue = 0;
            switch (type) {
                case SINE:
                    sampleValue = Math.sin(angle);
                    break;
                case SQUARE:
                    sampleValue = Math.sin(angle) >= 0 ? 1.0 : -1.0;
                    break;
                case TRIANGLE:
                    sampleValue = Math.asin(Math.sin(angle)) * (2.0 / Math.PI);
                    break;
                case SAWTOOTH:
                    sampleValue = 2.0 * (time * currentFreq - Math.floor(time * currentFreq + 0.5));
                    break;
            }
            double envelope = 1.0 - progress;
            buffer[i] = (byte) (sampleValue * volume * envelope * 127);
        }
        playRawAudio(buffer);
    }

    private void playRawAudio(byte[] buffer) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) return;

            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, buffer.length);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (Exception ignored) {
            // Audio line unavailable (e.g. headless environment)
        }
    }
}
