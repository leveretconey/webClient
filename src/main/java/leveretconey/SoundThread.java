package leveretconey;

import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class SoundThread  extends GrabberThread {
    private SoundGrabber grabber;
    SoundThread(LiveRecorder.SoundSource soundSource, FFmpegFrameRecorder recorder) {
        super(recorder);
        this.grabber= getGrabber(soundSource);
    }
    private SoundGrabber getGrabber(LiveRecorder.SoundSource soundSource){
        if(soundSource==null)
            return null;
        try {
            switch (soundSource){
                case MICROPHONE:
                    return new MicrophoneSoundGrabber();
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
        if (grabber!=null)
            grabber.start();
        Runnable runnable = ()->{
            while (!isStop()){
                try {
                    if(grabber==null) {
                        Thread.sleep(1);
                        continue;
                    }

                    ShortBuffer buffer=grabber.getSample();
                    long timestamp = getStartTimestamp();
                    synchronized (recorder) {
                        recorder.setTimestamp(timestamp);
                        recorder.recordSamples(buffer);
                    }
                    Thread.sleep(30);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                if(grabber!=null)
                    grabber.stop();
            }
            catch (RecorderException e){
            }
        };
        new Thread(runnable).start();
    }
    @SuppressWarnings("all")
    void setSoundSource(LiveRecorder.SoundSource soundSource){
        try {
            if(grabber!=null)
                grabber.stop();
            grabber=getGrabber(soundSource);
        }catch (RecorderException e){
            grabber=null;
        }
    }

}
