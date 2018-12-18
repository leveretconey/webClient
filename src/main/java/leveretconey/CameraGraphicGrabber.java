package leveretconey;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

class CameraGraphicGrabber extends GraphicGrabber {
    private static final int WEBCAM_DEVICE_INDEX=0;
    private OpenCVFrameGrabber grabber;
    @Override
    int getWidth() {
        return grabber.getImageWidth();
    }
    CameraGraphicGrabber() throws RecorderException {
        try {
            grabber=new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
            grabber.start();
        }catch (Exception e){
            throw new RecorderException("unable to start graphic thread");
        }
    }
    @Override
    void start(){
    }

    @Override
    void stop() {
        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            System.out.println("error when closing camera grabber");
        }
    }

    @Override
    Frame getFrame() throws RecorderException {
        try {
            return grabber.grab();
        } catch (Exception e) {
            throw new RecorderException("unable to grab frame");
        }
    }

    @Override
    int getHeight() {
        return grabber.getImageHeight();
    }
}
