package ai.issm.audiostreaming;

import static ai.issm.audiostreaming.Config.WEBSOCKET_URL;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
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

    private JSONObject jsonObject;

    private final PublishSubject<String> publishSubject = PublishSubject.create();
    private final PublishSubject<Boolean> publishIsRecording = PublishSubject.create();


    public WebSocketExample(Context context) {
        this.okHttpClient = new OkHttpClient();
        this.context = context;
        this.jsonObject = new JSONObject();
        this.run();


    }

    private void run() {
        Request request = new Request.Builder()
                .url(WEBSOCKET_URL)
                .build();


        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);
                System.out.println("Closing: " + code + " / " + reason);
                isConnectionOpen = false;
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosing(webSocket, code, reason);
                webSocket.close(1000, null);
                System.out.println("Closing: " + code + " / " + reason);
                Log.e("TAG", "WebSocket Failure: " + reason);
                isConnectionOpen = false;

            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                System.out.println("Error: " + t.getMessage());
                Log.e("TAG", "WebSocket Failure: " + t.getMessage());
                isConnectionOpen = false;
                publishSubject.onError(t);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    String code = jsonObject.getString("message");
                    publishSubject.onNext(code);
                } catch (JSONException e) {
                    publishSubject.onError(e);

                }
                System.out.println("Receiving an onMessage: " + text);


            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                publishSubject.onNext(bytes.hex());
                System.out.println("Receiving on onMessageHex: " + bytes.hex());
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                super.onOpen(webSocket, response);
                isConnectionOpen = true;
                System.out.println("Connection established: " + response);
            }
        };

        this.webSocket = okHttpClient.newWebSocket(request, webSocketListener);


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


    public void startSendingAudioPackets(boolean flag) {

        publishIsRecording.onNext(true);

        Completable.fromAction(() -> {
                    int sampleRateInHz = 44100;
                    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
                    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;


                    int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);


                    // Check if the microphone permission is granted
                    if (ActivityCompat.checkSelfPermission(this.context,
                            android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }


                    AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz,
                            channelConfig, audioFormat, bufferSize);

                    byte[] audioBuffer = new byte[1024];  // Adjust buffer size as necessary
                    audioRecord.startRecording();


                    int packetNumber = 0;
                    long startTime = System.currentTimeMillis();
                    long recordTime = 4000;  // Adjust recording time as necessary (e.g., 5000 for 5 seconds)

                    try {
                        while (System.currentTimeMillis() - startTime < recordTime) {
                            int bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);

                            if (bytesRead > 0) {
                                if (isConnectionOpen) {

                                    jsonObject = Utils.getAudioJSON(String.valueOf(packetNumber),
                                            Base64.encodeToString(audioBuffer, Base64.DEFAULT));
                                    webSocket.send(jsonObject.toString());
                                    packetNumber++;
                                }
                            }

                            Thread.sleep(500);  // 0.5-second interval
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "startSendingAudioPackets: " + e.getMessage());
                    }

                    jsonObject = Utils.getAudioJSON(String.valueOf(packetNumber), "EOS");
                    webSocket.send(jsonObject.toString());

                    System.out.println("Done sending audio packets");

                    audioRecord.stop();
                    audioRecord.release();
                    publishIsRecording.onNext(false);

                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> System.out.println("Done sending audio packets"),
                        (error) -> System.out.println("Error sending audio packets"));


    }

    public Observable<String> getObservableInstance() {
        return this.publishSubject.hide();

    }

    public Observable<Boolean> getObservableRecordingInstance() {
        return this.publishIsRecording.hide();
    }


}
