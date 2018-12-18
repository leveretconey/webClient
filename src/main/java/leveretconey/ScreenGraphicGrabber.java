package leveretconey;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

class ScreenGraphThread extends GraphicThread {
    private Rectangle rectangle;
    private Robot robot;
    private static final String FORMAT="jpg";
    private OpenCVFrameConverter.ToIplImage converter;
    @Override
    int getWidth() {
        return (int)rectangle.getWidth();
    }

    @Override
    int getHeight() {
        return (int)rectangle.getHeight();
    }

    @Override
    void start() throws RecorderException {
        try {
            Dimension screenSize   =  Toolkit.getDefaultToolkit().getScreenSize();
            rectangle=new Rectangle((int)screenSize.getWidth(),(int)screenSize.getHeight());
            robot=new Robot();
            converter = new OpenCVFrameConverter.ToIplImage();
            super.start();
        }catch (Exception e){
            throw new RecorderException("unable to start graphic thread");
        }
    }

    @Override
    protected Frame getFrame() throws RecorderException {
        try {
            BufferedImage image=robot.createScreenCapture(rectangle);
            Frame frame= convertBufferedImageIntoFrame(image);
            return frame;
        }catch (Exception e){
            throw new RecorderException("unable to grab frame");
        }
    }
    /*
     * 这里用了一种很傻的方法进行转换，就是将文件写入磁盘，然后再从磁盘里读出来
     * 这种方法性能极差，于是整个直播的帧率大概只有十帧
     * javacv自带的Java2dFrameConverter按理说对这个功能已经封装好了，但是我用它来转换总是有问题
     * 求大神改进
     */
    private Frame convertBufferedImageIntoFrame(BufferedImage image) throws RecorderException{
        try {
            File file = new File("out." + FORMAT);
            ImageIO.write(image, FORMAT, file);
            opencv_core.Mat mat = imread("out." + FORMAT);
            return converter.convert(mat);
        }
        catch (Exception e){
            throw new RecorderException("unable to grab frame");
        }
    }
}
