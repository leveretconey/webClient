package leveretconey;

abstract class Grabber {
    abstract void start() throws RecorderException;
    abstract void stop() throws RecorderException;
}
