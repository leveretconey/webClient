package leveretconey;

import org.bytedeco.javacv.Frame;

public interface FrameUpdateListener{
    void onUpdateFrame(Frame frame);
}