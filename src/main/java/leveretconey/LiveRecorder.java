package leveretconey;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

public class LiveRecorder {
    public enum GraphicSource {
        CAMERA,SCREEN
    }
    public enum SoundSource{
        MICROPHONE
    }
    private GraphicSource graphicSource;
    private GraphicThread graphicThread;
    private SoundSource soundSource;
    private SoundThread soundThread;
    private FFmpegFrameRecorder recorder;
    private long startTimestamp;
    private int width,height;
    public LiveRecorder(String output){
        this(output,640,480);
    }
    public LiveRecorder(String output,int width,int height){
        recorder = new FFmpegFrameRecorder(output, 2);
        recorder.setInterleaved(true);
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "25");
        recorder.setVideoBitrate(2000000);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(25);
        recorder.setGopSize(25 * 2);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(0);
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setImageWidth(width);
        recorder.setImageHeight(height);
    }
    public void start() throws RecorderException{
        try {
            recorder.start();
            startTimestamp=System.currentTimeMillis();
            startGraph();
            startSound();
        }

        catch (RecorderException e){
            throw e;
        } catch (FrameRecorder.Exception e) {
            throw new RecorderException("unable to start recorder");
        }
    }
    private void startGraph() throws RecorderException{
        if(graphicThread!=null && !graphicThread.isStop())
            graphicThread.stop();
        graphicThread =new GraphicThread(graphicSource,recorder);
        graphicThread.setStartTimestamp(startTimestamp);
        try {
            graphicThread.start();
        }catch (Exception e){
            graphicThread=null;
            throw new RecorderException("unable to start graph thread");
        }
    }
    private void startSound(){
        //todo
//        if(soundSource ==null){
//            return;
//        }
//        soundThread =SoundThreadFactory.create(soundSource);
//        try {
//            soundThread.start();
//        }catch (Exception e){
//            soundThread=null;
//            throw e;
//        }
    }
    private void stopGraph(){
        if(graphicThread!=null)
            graphicThread.stop();
    }
    private void stopSound() {
        if (soundThread != null) {
            soundThread.stop();
        }
    }
    private void stop(){
        stopGraph();
        stopSound();
    }
    public void destroy(){
        stop();
        try {
            recorder.stop();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("error when stopping thread");
        }
    }
    public void setGraphicSource(GraphicSource graphicSource) {
        this.graphicSource = graphicSource;
        if(graphicThread!=null)
            graphicThread.setGraphicSource(graphicSource);
    }

    public void setSoundSource(SoundSource soundSource) {
        this.soundSource = soundSource;
    }

}
