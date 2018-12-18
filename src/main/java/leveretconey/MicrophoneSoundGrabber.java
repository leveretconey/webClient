package leveretconey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

class MicrophoneSoundGrabber extends SoundGrabber{
    private AudioFormat audioFormat ;
    private TargetDataLine line;
    private int audioBufferSize;
    private byte[] audioBytes;

    MicrophoneSoundGrabber() throws RecorderException{
        try {
            audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100F, 16, 2, 4,
                            44100F, true);
            int sampleRate = (int) audioFormat.getSampleRate();
            int numChannels = audioFormat.getChannels();
            audioBufferSize = sampleRate * numChannels;
            audioBytes = new byte[audioBufferSize];
            Mixer mixer= AudioSystem.getMixer(AudioSystem.getMixerInfo()[2]);
            line=(TargetDataLine) AudioSystem.getLine(mixer.getTargetLineInfo()[0]);

        }
        catch (LineUnavailableException e){
            throw new RecorderException("unable to get sound line");
        }
    }
    @Override
    void start() throws RecorderException {
        try {
            line.open(audioFormat,audioBufferSize);
            line.start();
        }
        catch (LineUnavailableException e) {
            throw new RecorderException("unable to start sound line");
        }
    }

    @Override
    void stop() throws RecorderException {
        line.close();
    }

    @Override
    ShortBuffer getSample() {
        int nBytesRead = line.read(audioBytes, 0, line.available());
        int nSamplesRead = nBytesRead / 2;
        short[] samples = new short[nSamplesRead];
        ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
        return sBuff;
    }
}
