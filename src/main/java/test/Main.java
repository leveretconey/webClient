package test;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.GLCanvasFrame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import leveretconey.FrameUpdateListener;
import leveretconey.LiveRecorder;
import leveretconey.RecorderException;

class Main {
    JButton buttonStart;
    JButton buttonGraph;
    JButton buttonSound;
    JTextField textUrl;
    LiveRecorder recorder;
    Java2DFrameConverter converter;
    public static void main(String[] args) throws Exception {
        new Main().start();
    }
    void start() throws Exception{
        converter=new Java2DFrameConverter();
        JFrame canvasFrame=new JFrame();
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setSize(800,600);
        canvasFrame.setResizable(false);
        canvasFrame.setVisible(true);
        canvasFrame.setLayout(null);

        JPanel control=new JPanel( );
        canvasFrame.add(control);
        control.setBounds(0,500,800,80);
        PreviewPanel preview=new PreviewPanel();
        canvasFrame.add(preview);
        preview.setBounds(0,12,800,480);
        control.setLayout(null);
        control.setBackground(Color.gray);

        buttonStart=new JButton("启动");
        buttonGraph=new JButton("推送屏幕图像");
        buttonSound=new JButton("推送麦克风声音");
        textUrl=new JTextField("rtmp://me:1935/live/test");
        control.add(buttonGraph);
        control.add(buttonSound);
        control.add(buttonStart);
        control.add(textUrl);
        buttonGraph.setBounds(50,20,140,30);
        buttonSound.setBounds(210,20,140,30);
        buttonStart.setBounds(690,20,80,30);
        textUrl.setBounds(370,20,300,30);
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text=buttonStart.getText();
                if("停止".equals(text))
                {
                    if(recorder!=null)
                        recorder.destroy();
                    recorder=null;
                    buttonStart.setText("启动");
                }else {
                    try {
                        if (recorder == null) {
                            recorder = new LiveRecorder(textUrl.getText());
                            recorder.setGraphicSource(getGraphicSource());
                            recorder.setSoundSource(getSoundSource());
                            recorder.start();
                            recorder.setUpdateListener(new FrameUpdateListener() {
                                @Override
                                public void onUpdateFrame(Frame frame) {
                                    preview.repaint(converter.convert(frame));
                                }
                            });
                            buttonStart.setText("停止");
                        }
                    } catch (RecorderException error) {
                        error.printStackTrace();
                        if (recorder != null) {
                            recorder.destroy();
                            recorder = null;
                        }
                    }
                }
            }
        });
        buttonGraph.addActionListener((e)->{
            String text=buttonGraph.getText();
            String newtext;
            if("推送屏幕图像".equals(text)) {
                newtext="推送摄像头图像";
            }else if("推送摄像头图像".equals(text)){
                newtext="不推送图像";
            }else {
                newtext="推送屏幕图像";
            }
            buttonGraph.setText(newtext);
            if(recorder!=null)
                recorder.setGraphicSource(getGraphicSource());
        });
        buttonSound.addActionListener((e)->{
            String text=buttonSound.getText();
            String newtext;
            if("推送麦克风声音".equals(text)) {
                newtext="不推送声音";
            }else {
                newtext="推送麦克风声音";
            }
            buttonSound.setText(newtext);
            if(recorder!=null)
                recorder.setSoundSource(getSoundSource());
        });
    }
    private LiveRecorder.GraphicSource getGraphicSource(){
        String text=buttonGraph.getText();
        if("推送屏幕图像".equals(text)) {
            return LiveRecorder.GraphicSource.SCREEN;
        }else if("推送摄像头图像".equals(text)){
            return LiveRecorder.GraphicSource.CAMERA;
        }else {
            return null;
        }
    }
    private LiveRecorder.SoundSource getSoundSource(){
        String text=buttonSound.getText();
        if("推送麦克风声音".equals(text)) {
            return LiveRecorder.SoundSource.MICROPHONE;
        }else {
            return null;
        }
    }
}

class PreviewPanel extends JPanel{
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(image==null)
            return;
        double scale=Math.max((double)image.getHeight(null)/this.getHeight()
        ,(double)image.getWidth(null)/this.getWidth());
        scale=1/scale;
        AffineTransformOp op=new AffineTransformOp(AffineTransform
                .getScaleInstance(scale,scale),null);
        image=op.filter(image,null);
        g.drawImage(image,0,0,null);
    }
    private BufferedImage image;
    public void repaint(BufferedImage image){
        this.image=image;
        repaint();
    }
}

