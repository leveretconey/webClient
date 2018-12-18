package leveretconey;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

class CameraGraphicThread extends GraphicThread {
    private static final int WEBCAM_DEVICE_INDEX=0;
    private OpenCVFrameGrabber grabber;
    @Override
    int getWidth() {
        return grabber.getImageWidth();
    }

    @Override
    void start() throws RecorderException {
        try {
            grabber=new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
            super.start();
        }catch (Exception e){
            throw new RecorderException("unable to start graphic thread");
        }

    }

    @Override
    void stop() {
        super.stop();
        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            System.out.println("error when closing camera grabber");
        }

    }

    @Override
    protected Frame getFrame() throws RecorderException {
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
