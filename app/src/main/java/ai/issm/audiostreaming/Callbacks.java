package ai.issm.audiostreaming;

public interface Callbacks {
    void onAudioStreamStarted();

    void onAudioStreamStopped();

    void onResponseReceived(String response) throws InterruptedException;
}
