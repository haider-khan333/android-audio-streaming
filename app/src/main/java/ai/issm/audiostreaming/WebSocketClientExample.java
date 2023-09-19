package ai.issm.audiostreaming;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketClientExample extends WebSocketClient {

    private static final String TAG = "WebSocketClientExample";

    public WebSocketClientExample(URI serverURI) {
        super(serverURI);
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i(TAG, "onOpen: connection opened");

    }

    @Override
    public void onMessage(String message) {
        Log.i(TAG, "onMessage: message received: " + message);


    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "onClose: connection closed");


    }

    @Override
    public void onError(Exception ex) {
        Log.i(TAG, "onError: error: " + ex.getMessage());

    }
}
