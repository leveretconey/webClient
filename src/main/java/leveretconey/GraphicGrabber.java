package leveretconey;

import org.bytedeco.javacv.Frame;

abstract class GraphicGrabber extends Grabber{
    abstract int getHeight();
    abstract int getWidth();
    abstract Frame getFrame() throws RecorderException;
}
