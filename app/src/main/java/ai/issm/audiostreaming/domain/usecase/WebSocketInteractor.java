package ai.issm.audiostreaming.domain.usecase;


import android.content.Context;

import ai.issm.audiostreaming.WebSocketExample;
import io.reactivex.rxjava3.core.Observable;

public class WebSocketInteractor {

    private WebSocketExample webSocketExample;
    private Context context;

    public WebSocketInteractor(Context context) {
        this.context = context;

    }

    public void initialize() {
        if (this.webSocketExample != null) {
            this.webSocketExample.cleanUp();
        }

        this.webSocketExample = new WebSocketExample(context);

    }

    public void startSendingAudioPackets() {
        this.webSocketExample.startSendingAudioPackets();
    }


    public Observable<String> getMessageStream() {
        return this.webSocketExample.getObservableInstance();
    }

    public Observable<Boolean> getRecordingStatusStream() {
        return this.webSocketExample.getObservableRecordingInstance();
    }

    public void cleanUp() {
        this.webSocketExample.cleanUp();
    }
}
