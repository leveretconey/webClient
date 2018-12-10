package test;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.bytedeco.javacpp.opencv_core.*;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

public class Test {
    static Robot robot;
    public static void main(String[] args) throws Exception, InterruptedException {
        Loader.load(opencv_objdetect.class);
        recordCamera("rtmp://me:1935/live/test",20);
    }
    static long startTime=0;
    static  long videoTS=0;
    public static void recordCamera(String outputFile, double frameRate)
            throws Exception, InterruptedException, org.bytedeco.javacv.FrameRecorder.Exception {

        LinkedList<opencv_core.IplImage> frames=new LinkedList<>();
        Runnable runnable=new CodingAndSend();
        ((CodingAndSend) runnable).frames=frames;
        new Thread(runnable).start();
        robot=new Robot();
        int a=0;
        long lastFrame=System.currentTimeMillis();
        while (a>=0) {
            a++;
            BufferedImage screenCapture = robot.createScreenCapture(new Rectangle(1366,768));
            File f = new File("printscreen.JPEG");
            ImageIO.write(screenCapture, "JPEG", f);
            opencv_core.IplImage image = cvLoadImage("printscreen.JPEG");
            synchronized (frames){
                frames.offer(image);
            }
            long interval=(System.currentTimeMillis()-lastFrame);
            System.out.println("capture:"+interval);
            lastFrame=System.currentTimeMillis();
            if(interval<50)
                Thread.sleep(50-interval);
        }
    }

    public static opencv_core.Mat BufImg2Mat (BufferedImage original) {
        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        MatExpr mat = opencv_core.Mat.zeros(original.getHeight(), original.getWidth(), opencv_core.CV_32F);
        Mat mymat=mat.asMat();
        mymat.

    }
}

class CodingAndSend implements Runnable{
    LinkedList<opencv_core.IplImage> frames;
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
                opencv_core.IplImage frame=null;
                synchronized (frames){
                    if(!frames.isEmpty()){
                        frame=frames.poll();
                    }
                }
                if(frame!=null){
                    Frame rotatedFrame = converter.convert(frame);
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


