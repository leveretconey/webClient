package test;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.GLCanvasFrame;

import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import leveretconey.FrameUpdateListener;
import leveretconey.LiveRecorder;

class Main {
    BufferedImage background;
    public static void main(String[] args) throws Exception {
        new Main().start();
    }
    void start() throws Exception{
        CanvasFrame canvasFrame = new CanvasFrame("HelloWorldSwing",
                CanvasFrame.getDefaultGamma()/2.2);
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setSize(800,600);
        canvasFrame.setResizable(false);
        canvasFrame.setVisible(true);

        JButton buttonStart=new JButton("启动");
        JButton buttonGraph=new JButton("推送屏幕图像");
        JButton buttonSound=new JButton("推送麦克风声音");
        JTextField textUrl=new JTextField("rtmp://me:1935/live/test");

        LiveRecorder recorder=new LiveRecorder("rtmp://me:1935/live/test",1366,768);
        recorder.setSoundSource(LiveRecorder.SoundSource.MICROPHONE);
        recorder.setGraphicSource(LiveRecorder.GraphicSource.SCREEN);
        recorder.setUpdateListener(new FrameUpdateListener() {
            @Override
            public void onUpdateFrame(Frame frame) {
                if(canvasFrame.isVisible())
                    canvasFrame.showImage(frame);
            }
        });
        recorder.start();
        while (true);
    }
}

