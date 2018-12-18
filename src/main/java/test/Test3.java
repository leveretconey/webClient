package test;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

class Test3 {
    public static void main(String[] args) throws Exception {
        Loader.load(opencv_objdetect.class);
        recordWebcamAndMicrophone(0,4,
                "rtmp://me:1935/live/test",640,480,25);
    }
    public static void recordWebcamAndMicrophone(int WEBCAM_DEVICE_INDEX, int AUDIO_DEVICE_INDEX, String outputFile,
                                                 int captureWidth, int captureHeight, int FRAME_RATE) throws Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
        grabber.setImageWidth(captureWidth);
        grabber.setImageHeight(captureHeight);
        grabber.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, captureWidth, captureHeight, 2);
        recorder.setInterleaved(true);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "25");
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(FRAME_RATE);
        recorder.setGopSize(FRAME_RATE * 2);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(0);
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.start();
        final long startTime = System.currentTimeMillis();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                try {
                    TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                    line.open(audioFormat);
                    line.start();
                    int sampleRate = (int) audioFormat.getSampleRate();
                    int numChannels = audioFormat.getChannels();
                    int audioBufferSize = sampleRate * numChannels;
                    byte[] audioBytes = new byte[audioBufferSize];

                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    exec.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int nBytesRead = line.read(audioBytes, 0, line.available());
                                int nSamplesRead = nBytesRead / 2;
                                short[] samples = new short[nSamplesRead];
                                ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                                long videoTS = 1000 * (System.currentTimeMillis() - startTime);
                                if (videoTS > recorder.getTimestamp()) {
                                    recorder.setTimestamp(videoTS);
                                }
                                recorder.recordSamples(sampleRate, numChannels, sBuff);
                            } catch (FrameRecorder.Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, (long) 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);
                } catch (LineUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        };
        CanvasFrame cFrame = new CanvasFrame("Capture Preview", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        Frame capturedFrame = null;
        new Thread(runnable).start();

        while ((capturedFrame = grabber.grab()) != null) {
            if (cFrame.isVisible()) {
                cFrame.showImage(capturedFrame);
            }
            long videoTS = 1000 * (System.currentTimeMillis() - startTime);
            if (videoTS > recorder.getTimestamp()) {
                recorder.setTimestamp(videoTS);
            }
            recorder.record(capturedFrame);
        }
        cFrame.dispose();
        recorder.stop();
        grabber.stop();
    }
}
