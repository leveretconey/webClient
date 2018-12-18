package leveretconey;

import org.bytedeco.javacv.FFmpegFrameRecorder;

abstract class GrabberThread {
    private boolean stop=false;
    protected FFmpegFrameRecorder recorder;
    protected long startTimestamp;
    protected GrabberThread(FFmpegFrameRecorder recorder){
        this.recorder = recorder;
    }
    void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
    void stop(){
        stop=true;
    }
    boolean isStop(){
        return stop;
    }
    void start() throws RecorderException{
        if(recorder==null)
            throw new RecorderException("recorder not set");
        if(startTimestamp==0)
            throw new RecorderException("timestamp not set");
    }
    protected long getStartTimestamp(){
        return 1000 * (System.currentTimeMillis() - startTimestamp);
    }
}
