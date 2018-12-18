package leveretconey;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

class GraphicThread  extends GrabberThread {
    private GraphicGrabber grabber;
    GraphicThread(LiveRecorder.GraphicSource graphicSource, FFmpegFrameRecorder recorder) {
        super(recorder);
        this.grabber= getGrabber(graphicSource);
    }
    private FrameUpdateListener updateListener;

    public void setUpdateListener(FrameUpdateListener updateListener) {
        this.updateListener = updateListener;
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
                    if(updateListener!=null)
                        updateListener.onUpdateFrame(frame);
                    long timestamp = getStartTimestamp();
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
