package ai.issm.audiostreaming;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ai.issm.audiostreaming.domain.usecase.WebSocketInteractor;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private WebSocketInteractor webSocketInteractor;


    // UI Variables
    private TextView responseTextView;
    private TextView recordingStatusTextView;
    private Button button;

    private String message;

    // Custom Variables
    private WebSocketExample webSocketExample;
    private PermissionHandler permissionHandler;

    // Local Variables
    private int AUDIO_PERMISSION_REQ_CODE = 1;
    private final String TAG = "WebSocketClientExample";

    private Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");


        setContentView(R.layout.activity_main);
        button = findViewById(R.id.recordButton);
        recordingStatusTextView = findViewById(R.id.recordingStatusTextView);
        responseTextView = findViewById(R.id.responseMessageTextView);


        permissionHandler = new PermissionHandler(this);

//        webSocketExample = new WebSocketExample(this);
//        webSocketInteractor = new WebSocketInteractor(webSocketExample);
        button.setOnClickListener(this);

        initializeWebSocket();

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart");
        initializeWebSocket();

    }

    private void initializeWebSocket() {
        if (webSocketInteractor != null)
            webSocketInteractor.cleanUp();

        webSocketInteractor = new WebSocketInteractor(this);


        if (disposable != null)
            disposable.dispose();


        disposable = webSocketInteractor.getMessageStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleMessage, this::handleError);

        disposable = webSocketInteractor.getRecordingStatusStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleRecordingStatus, this::handleError);
    }


    private void handleMessage(String message) {
        Intent intent = new Intent(this, ResponseActivity.class);
        intent.putExtra("message", message);
        startActivity(intent);
    }

    private void handleError(Throwable error) {
        Log.i(TAG, "handleError: " + error);
    }

    private void handleRecordingStatus(Boolean isRecording) {
        if (isRecording) {
            recordingStatusTextView.setText("Recording...");
            button.setEnabled(false);
        } else {
            recordingStatusTextView.setText("Not Recording");
            button.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == AUDIO_PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Audio Permission Granted");

            } else {
                Log.i(TAG, "onRequestPermissionsResult: Audio Permission Denied");
            }
        }
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: button clicked");
        if (permissionHandler.hasRecordAudioPermission()) {
            Log.i(TAG, "onClick: permission already granted");
            button.setEnabled(false);
//            webSocketExample.startSendingAudioPackets();
            webSocketInteractor.startSendingAudioPackets();


        } else {
            Log.i(TAG, "onClick: requesting permission");
            permissionHandler.requestRecordAudioPermission(this, AUDIO_PERMISSION_REQ_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (disposable != null)
            disposable.dispose();

        webSocketInteractor.cleanUp();


    }
}