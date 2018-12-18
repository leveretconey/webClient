package test;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.sound.sampled.Port;


public class Test {
    static Robot robot;
    public static void main(String[] args) throws Exception, InterruptedException {
        Loader.load(opencv_objdetect.class);
        new Test().recordCamera("rtmp://me:1935/live/test",20);
    }
    public void recordCamera(String outputFile, double frameRate)
            throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {

        LinkedList<String> frames=new LinkedList<>();
        Runnable runnable=new CodingAndSend();
        ((CodingAndSend) runnable).frames=frames;
        new Thread(runnable).start();
        robot=new Robot();
        int a=0;
        long lastFrame=System.currentTimeMillis();
        while (a>=0) {
            a++;

            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(1366,768));
            long time=System.currentTimeMillis();
            File file=new File(time+".jpeg");
            ImageIO.write(screenCapture,"jpeg",file);
            synchronized (frames){
                frames.offer(String.valueOf(System.currentTimeMillis()));
            }
            long interval=(System.currentTimeMillis()-lastFrame);
            System.out.println("capture:"+interval);
            lastFrame=System.currentTimeMillis();
            if(interval<50)
                Thread.sleep(50-interval);
        }
    }
}

class CodingAndSend implements Runnable{
    LinkedList<String> frames;
    boolean stop=false;
    @Override
    public void run() {
        try {
            long startTime=0,videoTS=0;
            OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();//转换器
            FrameRecorder recorder = FrameRecorder.createDefault("rtmp://me:1935/live/test", 1366, 768);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264，编码
            recorder.setFormat("flv");//封装格式，如果是推送到rtmp就必须是flv封装格式
            recorder.setFrameRate(20);
            recorder.start();
            long lastFrame=System.currentTimeMillis();
            while (!stop) {
                String file=null;
                synchronized (frames){
                    if(!frames.isEmpty()){
                        file=frames.poll();
                    }
                }
                if(file!=null){
                    Frame rotatedFrame=converter.convert(new opencv_core.IplImage());

                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }
                    videoTS = 1000 * (System.currentTimeMillis() - startTime);
                    recorder.setTimestamp(videoTS);
                    recorder.record(rotatedFrame);
                    System.out.println("code:"+(System.currentTimeMillis()-lastFrame));
                    lastFrame=System.currentTimeMillis();
                }
            }
            recorder.stop();
            recorder.release();
        }catch (Exception e){}
    }
}

