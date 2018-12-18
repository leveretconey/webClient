package leveretconey;

import java.nio.ShortBuffer;

abstract class SoundGrabber extends Grabber{
    abstract ShortBuffer getSample();
}
