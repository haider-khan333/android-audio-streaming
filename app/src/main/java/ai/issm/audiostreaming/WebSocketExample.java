package ai.issm.audiostreaming;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketExample {
    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private Boolean isConnectionOpen = false;

    private String TAG = "WebSocketExample";


    private Context context;

    private long startTime = System.currentTimeMillis();
    private long recordingDurationMs = 6000; // 6 seconds recording duration as an example
    private long packetIntervalMs = 500; // 0.5 seconds interval for sending packets
    private int packetNumber = 0;

    private static final int SAMPLE_RATE_IN_HZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;


    public WebSocketExample(Context context) {
        this.okHttpClient = new OkHttpClient();
        this.context = context;
        run();
    }

    private void run() {
        Request request = new Request.Builder()
                .url("ws://192.168.18.22:8000/xyz")
                .build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                System.out.println("Closing: " + code + " / " + reason);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                System.out.println("Closing: " + code + " / " + reason);
                Log.e("TAG", "WebSocket Failure: " + reason);
                isConnectionOpen = false;
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                System.out.println("Error: " + t.getMessage());
                Log.e("TAG", "WebSocket Failure: " + t.getMessage());
                isConnectionOpen = false;
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                System.out.println("Receiving: " + text);
                if (text.equals("Hello from server")) {
                    webSocket.send("Hello from haider");
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                System.out.println("Receiving: " + bytes.hex());
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                isConnectionOpen = true;
//                webSocket.send("Conversation started");
//                webSocket.send("Hello from haider");
            }
        };

        this.webSocket = okHttpClient.newWebSocket(request, webSocketListener);


    }

    public void sendMessage(String message) {

        if (this.webSocket == null) {

            return;
        }
        if (isConnectionOpen) {
            this.webSocket.send(message);
        } else {
            Log.e("TAG", "WebSocket Failure: Connection is not open");
        }
    }

    public void cleanUp() {
        this.okHttpClient.dispatcher().executorService().shutdown();
    }


    private byte[] getAudioFileData() {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.audio);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, bytesRead);
            }
            inputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void startSendingAudioPackets() {
        byte[] audioData = getAudioFileData();
        if (audioData == null) {
            Log.e(TAG, "Failed to read audio file");
            return;
        }

        int packetSize = 1024; // You might want to choose a different size
        int packetNumber = 0;

        for (int i = 0; i < audioData.length; i += packetSize) {
            final int end = Math.min(i + packetSize, audioData.length);
            final byte[] packet = Arrays.copyOfRange(audioData, i, end);

            if (isConnectionOpen) {
                webSocket.send("Packet " + packetNumber + ": " + Base64.encodeToString(packet, Base64.DEFAULT));
                packetNumber++;
            } else {
                Log.e(TAG, "WebSocket Failure: Connection is not open");
            }

            try {
                Thread.sleep(500); // 0.5-second interval
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "startSendingAudioPackets: " + e.getMessage());
            }
        }
    }


    public void startRecordingAndStreaming() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (ActivityCompat.checkSelfPermission(this.context,
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        byte[] audioBuffer = new byte[bufferSize];
        audioRecord.startRecording();


        int packetNumber = 0;

        while (System.currentTimeMillis() - startTime < recordingDurationMs) {
            int bytesRead = audioRecord.read(audioBuffer, 0, bufferSize);

            if (bytesRead > 0) {
                // Send audio data with packet number
                webSocket.send("Packet " + packetNumber + ": " +
                        Base64.encodeToString(audioBuffer, Base64.DEFAULT));
                packetNumber++;
            }

            // Sleep for the packet interval before the next iteration
            try {
                Thread.sleep(packetIntervalMs);
            } catch (InterruptedException e) {
                // Handle the interruption appropriately
                e.printStackTrace();
                Log.e("TAG", "startRecordingAndStreaming: " + e.getMessage());
            }
        }

        // Poll for the response
        // ... (implement polling logic to get processed response from server)

        audioRecord.stop();
        audioRecord.release();
    }
}
