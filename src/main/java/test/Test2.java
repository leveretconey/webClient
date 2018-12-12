package test;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.JavaCV;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Target;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

class Test2 {
    static  FFmpegFrameRecorder recorder;

    public static void main(String[] args) throws Exception {
        test();
        Loader.load(opencv_objdetect.class);
        recorder = new FFmpegFrameRecorder("rtmp://me:1935/live/test", 1366, 768,2);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("flv");
        recorder.setFrameRate(25);
        recorder.start();//开启录制器

//        picExperiment();

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        Runnable crabAudio = catchAudio(4, 25);//对应上面的方法体
        ScheduledFuture tasker = exec.scheduleAtFixedRate(crabAudio, 0, (long) 1000 / 25,
                TimeUnit.MILLISECONDS);
        while (true);
    }
    static void test(){
        if (true)
            return;
        for(Mixer.Info mInfo : AudioSystem.getMixerInfo()){
            System.out.print(mInfo.getDescription()+"\n");
            Mixer mixer=AudioSystem.getMixer(mInfo);
            for(Line.Info info:mixer.getSourceLineInfo()){
                System.out.println(info.toString());
            }
            System.out.println("-------------------------------");
            for(Line.Info info:mixer.getTargetLineInfo()){
                System.out.println(info.toString());
            }
            System.out.print("\n\n");
        }
        System.exit(1);
    }
    static void picExperiment() throws Exception{
        Runnable runnable = new Runnable() {
            public void run() {

                try {

                    OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器
                    int width = 1366;
                    int height = 768;
                    Robot robot=new Robot();
                    Rectangle rectangle=new Rectangle(width,height);
                    long startTime=0;
                    long videoTS=0;
                    Frame rotatedFrame;
                    while (true) {
                        BufferedImage bi=robot.createScreenCapture(rectangle);
                        String format="jpg";
                        File file=new File("out."+format);
                        ImageIO.write(bi,format,file);
                        opencv_core.Mat image=imread("out."+format);
                        rotatedFrame=converter.convert(image);
                        if (startTime == 0) {
                            startTime = System.currentTimeMillis();
                        }

                        videoTS = 1000 * (System.currentTimeMillis() - startTime);
                        recorder.setTimestamp(videoTS);
                        recorder.record(rotatedFrame);
                        image.release();
                    }
                } catch (AWTException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        runnable.run();
    }
    static Runnable catchAudio(int unknown,int framerate)throws Exception{
        AudioFormat audioFormat =
                new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100F, 16, 2, 4,
                        44100F, true);
        int sampleRate = (int) audioFormat.getSampleRate();
        int numChannels = audioFormat.getChannels();
        int audioBufferSize = sampleRate * numChannels;
        byte[] audioBytes = new byte[audioBufferSize];
        Mixer mixer=AudioSystem.getMixer(AudioSystem.getMixerInfo()[2]);
        TargetDataLine line=(TargetDataLine) AudioSystem.getLine(mixer.getTargetLineInfo()[0]);
//        DataLine.Info info=new DataLine.Info(TargetDataLine.class,audioFormat);
//        TargetDataLine line=(TargetDataLine) AudioSystem.getLine(info);
        line.open();
        line.start();
        Runnable crabAudio = new Runnable() {
            ShortBuffer sBuff = null;
            int nBytesRead;
            int nSamplesRead;

            @Override
            public void run() {
                try {
                    System.out.println(line.available());
                    nBytesRead = line.read(audioBytes, 0, line.available());
                    nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
                    recorder.recordSamples(sampleRate, numChannels, sBuff);
                    System.out.println("sent");
                }
                catch (Exception e){e.printStackTrace();};

            }

            @Override
            protected void finalize() throws Throwable {
                sBuff.clear();
                sBuff = null;
                super.finalize();
            }
        };
        return crabAudio;

    }
}
