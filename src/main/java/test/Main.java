package leveretconey;

class Main {
    public static void main(String[] args) throws Exception {
        LiveRecorder recorder=new LiveRecorder("rtmp://me:1935/live/test");
        recorder.setGraphicSource(LiveRecorder.GraphicSource.SCREEN);
        recorder.start();
        Thread.sleep(15000);
        recorder.setGraphicSource(null);
        Thread.sleep(10000);
        recorder.setGraphicSource(LiveRecorder.GraphicSource.CAMERA);
        Thread.sleep(20000);
        recorder.destroy();
        System.out.println("end");
    }
}
