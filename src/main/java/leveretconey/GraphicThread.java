package leveretconey;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

class GraphicThread  extends GrabberThread {
    private GraphicGrabber grabber;
    GraphicThread(LiveRecorder.GraphicSource graphicSource, FFmpegFrameRecorder recorder) {
        super(recorder);
        this.grabber= getGrabber(graphicSource);
    }
    private GraphicGrabber getGrabber(LiveRecorder.GraphicSource graphicSource){
        if(graphicSource==null)
            return null;
        try {
            switch (graphicSource){
                case SCREEN:
                    return new ScreenGraphicGrabber();
                case CAMERA:
                    return new CameraGraphicGrabber();
                default:
                    return null;
            }
        }catch (RecorderException e){
            return null;
        }


    }

    @Override
    void start() throws RecorderException{
        super.start();
        if(grabber==null) {
            return;
        }
        grabber.start();
        Runnable runnable = ()->{
            while (!isStop()){
                try {
                    if(grabber==null) {
                        Thread.sleep(1);
                        continue;
                    }
                    Frame frame=grabber.getFrame();
                    if(frame==null)
                        continue;
                    long timestamp = 1000 * (System.currentTimeMillis() - startTimestamp);
                    synchronized (recorder) {
                        recorder.setTimestamp(timestamp);
                        recorder.record(frame);
                    }
                }
                catch (Exception e) {
                }
            }
            try {
                grabber.stop();
            }
            catch (RecorderException e){
            }
        };
        new Thread(runnable).start();
    }
    void setGraphicSource(LiveRecorder.GraphicSource graphicSource){
        grabber=getGrabber(graphicSource);
    }

}
